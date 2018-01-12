package com.suming.plugin;

import com.intellij.lang.ecmascript6.psi.ES6Class;
import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileContentsChangedAdapter;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileManagerListener;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.*;
import com.suming.plugin.bean.Component;
import com.suming.plugin.bean.ESVersion;
import com.suming.plugin.bean.ImportMode;
import com.suming.plugin.bean.PropTypeBean;
import com.suming.plugin.ui.PropTypesDialog;
import com.suming.plugin.utils.PsiElementHelper;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class PropTypeAction extends CommonAction {

  @Override
  void actionPerformed(Project project,
                       Editor editor,
                       PsiFile file,
                       String selectedText,
                       List<PropTypeBean> propNameList,
                       Component component) {
    PropTypesDialog dialog = new PropTypesDialog(propNameList, component);
    dialog.pack();
    dialog.setLocationRelativeTo(WindowManager.getInstance().getFrame(project));
    dialog.setOnSubmitListener((beans, importMode, esVersion, handleDefault) -> {
      Document document = editor.getDocument();

      runCommand(project, () -> {
        //insert PropTypes Object
        insertPropTypesCodeString(document, file, selectedText, beans, esVersion);
        //insert import statement
        autoInsertImportPropTypes(document, file, importMode);
        //insert defaultProps Object
        if (handleDefault) {
          List<PropTypeBean> defaultBeans = beans.stream()
                  .filter(o -> o.getDefaultValue() != null && !o.getDefaultValue().trim().equals(""))
                  .collect(Collectors.toList());
          if(defaultBeans.size() > 0){
            PsiFile nextFile = PsiFileFactory.getInstance(project).createFileFromText(file.getLanguage(), document.getText());
            insertDefaultPropsCodeString(document, nextFile, selectedText, defaultBeans, esVersion);
          }
        }
      });
    });
    dialog.setVisible(true);
  }


  private void autoInsertImportPropTypes(Document document, PsiFile file, ImportMode importMode) {

    if (importMode == ImportMode.Disabled) return;
    boolean isNew = importMode == ImportMode.NewModules;

    if (!hasImportPropTypes(file)) {
      int firstImportIndex = findFirstImportIndex(file);
      if (isNew) {
        document.insertString(firstImportIndex, "import PropTypes from \'prop-types\'\n");
      } else {
        ES6ImportDeclaration reactImport = getReactImportDeclaration(file);
        if (reactImport == null) {
          document.insertString(firstImportIndex, "import React, {PropTypes} from \'react\'\n");
        } else {
          if (reactImport.getFromClause() == null) return;
          PsiElement pFrom = reactImport.getFromClause();
          if (pFrom.getPrevSibling() == null || pFrom.getPrevSibling().getPrevSibling() == null) return;
          PsiElement p1 = pFrom.getPrevSibling();
          PsiElement p2 = p1.getPrevSibling();
          PsiElement rbrace = null;
          if (p1.getText().equals("}")) {
            rbrace = p1;
          } else if (p2.getText().equals("}")) {
            rbrace = p2;
          }
          if (rbrace != null) {
            int index = rbrace.getTextRange().getStartOffset();
            boolean isNeedComma = reactImport.getImportSpecifiers().length > 0;
            document.insertString(index, isNeedComma ? ",PropTypes" : "PropTypes");
          } else {
            int index = pFrom.getTextRange().getStartOffset();
            PsiElement p = PsiElementHelper.getRealPreElement(pFrom);
            boolean isNeedComma = p != null && !p.getText().equals(",");
            document.insertString(index, isNeedComma ? ",{ PropTypes } " : "{ PropTypes }");
          }
        }

      }
    }
  }

  private void insertDefaultPropsCodeString(Document document, PsiFile file, String componentName,
                                            List<PropTypeBean> beans, ESVersion esVersion) {
    PsiElement es7Element = getES7FieldElementByName(file, componentName, "defaultProps");
    PsiElement es6Element = getES6FieldByName(file, componentName, "defaultProps");
    boolean isES7 = esVersion == ESVersion.ES7;
    if (isES7 && es7Element == null) {
      ES6Class es6Class = getSelectES6Component(componentName, file);
      if (es6Class != null) {
        PsiElement p = PsiElementHelper.getRealFirstChild(es6Class);
        if (p != null) {
          TextRange pRange = p.getTextRange();
          document.insertString(pRange.getStartOffset(),
                  getInsertDefaultPropsString(componentName, beans,
                          true, true) + "\n\n  ");
        }
      }
    } else if (!isES7 && es6Element == null) {
      PsiElement p = file.getLastChild();
      TextRange pRange = p.getTextRange();
      if (p instanceof PsiWhiteSpace) {
        document.replaceString(pRange.getStartOffset(), pRange.getEndOffset(),
                getInsertDefaultPropsString(componentName, beans,
                        true, false));
      } else {
        document.insertString(pRange.getEndOffset(),
                getInsertDefaultPropsString(componentName, beans,
                        true, false));
      }
    } else {
      TextRange textRange = (isES7 ? es7Element : es6Element).getLastChild().getTextRange();
      document.replaceString(textRange.getStartOffset(), textRange.getEndOffset(),
              getInsertDefaultPropsString(componentName, beans, false, isES7));
    }
  }

  private void insertPropTypesCodeString(Document document, PsiFile file, String componentName,
                                         List<PropTypeBean> beans, ESVersion esVersion) {
    PsiElement es7Element = getES7FieldElementByName(file, componentName, "propTypes");
    PsiElement es6Element = getES6FieldByName(file, componentName, "propTypes");
    boolean isES7 = esVersion == ESVersion.ES7;
    if (isES7 && es7Element == null) {
      ES6Class es6Class = getSelectES6Component(componentName, file);
      if (es6Class != null) {
        PsiElement p = PsiElementHelper.getRealFirstChild(es6Class);
        if (p != null) {
          TextRange pRange = p.getTextRange();
          document.insertString(pRange.getStartOffset(),
                  getInsertPropTypeCodeString(componentName, beans,
                          true, true) + "\n\n  ");
        }
      }
    } else if (!isES7 && es6Element == null) {
      PsiElement p = file.getLastChild();
      TextRange pRange = p.getTextRange();
      if (p instanceof PsiWhiteSpace) {
        document.replaceString(pRange.getStartOffset(), pRange.getEndOffset(),
                getInsertPropTypeCodeString(componentName, beans,
                        true, false));
      } else {
        document.insertString(pRange.getEndOffset(),
                getInsertPropTypeCodeString(componentName, beans,
                        true, false));
      }
    } else {
      TextRange textRange = (isES7 ? es7Element : es6Element).getLastChild().getTextRange();
      document.replaceString(textRange.getStartOffset(), textRange.getEndOffset(),
              getInsertPropTypeCodeString(componentName, beans, false, isES7));
    }
  }

  private String getInsertPropTypeCodeString(String componentName, List<PropTypeBean> beans,
                                             boolean isNewPropTypes, boolean isES7) {
    StringBuilder sb = new StringBuilder();
    if (isNewPropTypes) {
      if (isES7) {
        sb.append("static propTypes = {\n");
      } else {
        sb.append("\n\n");
        sb.append(componentName).append(".propTypes = {\n");
      }
    } else {
      sb.append("{\n");
    }
    for (int i = 0; i < beans.size(); i++) {
      sb.append(isES7 ? "    " : "  ").append(beans.get(i).name).append(": PropTypes.").append(beans.get(i).type);
      if (beans.get(i).isRequired) {
        sb.append(".isRequired");
      }
      if (i < beans.size() - 1) sb.append(",\n");
    }
    sb.append("\n").append(isES7 ? "  " : "").append("}");
    return sb.toString();
  }

  private String getInsertDefaultPropsString(String componentName, List<PropTypeBean> beans,
                                             boolean isNewPropTypes, boolean isES7) {
    StringBuilder sb = new StringBuilder();
    if (isNewPropTypes) {
      if (isES7) {
        sb.append("static defaultProps = {\n");
      } else {
        sb.append("\n\n");
        sb.append(componentName).append(".defaultProps = {\n");
      }
    } else {
      sb.append("{\n");
    }
    for (int i = 0; i < beans.size(); i++) {
      sb.append(isES7 ? "    " : "  ").append(beans.get(i).name).append(": ").append(beans.get(i).getDefaultValue());
      if (i < beans.size() - 1) sb.append(",\n");
    }
    sb.append("\n").append(isES7 ? "  " : "").append("}");
    return sb.toString();
  }
}

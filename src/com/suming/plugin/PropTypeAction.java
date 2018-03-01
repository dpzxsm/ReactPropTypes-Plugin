package com.suming.plugin;

import com.intellij.lang.ecmascript6.psi.ES6Class;
import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiWhiteSpace;
import com.suming.plugin.bean.*;
import com.suming.plugin.persist.SettingService;
import com.suming.plugin.ui.PropTypesDialog;
import com.suming.plugin.utils.PropTypesHelper;
import com.suming.plugin.utils.PsiElementHelper;

import java.util.List;
import java.util.stream.Collectors;

public class PropTypeAction extends CommonAction {
  private SettingService settingService = ServiceManager.getService(SettingService.class);

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
      TextRange textRange = (isES7 ? es7Element : es6Element).getParent().getTextRange();
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
      TextRange textRange = (isES7 ? es7Element : es6Element).getParent().getTextRange();
      document.replaceString(textRange.getStartOffset(), textRange.getEndOffset(),
              getInsertPropTypeCodeString(componentName, beans, false, isES7));
    }
  }

  private String getInsertPropTypeCodeString(String componentName, List<PropTypeBean> beans,
                                             boolean isNewPropTypes, boolean isES7) {
    int indent = settingService.getState().getIndent();
    boolean noSemiColons = settingService.getState().isNoSemiColons();
    StringBuilder sb = new StringBuilder();
    String propsObjBlank = isES7 ? "  " : "";
    String propsBlank = propsObjBlank + PropTypesHelper.getBlank(indent);
    if (isES7) {
      sb.append("static propTypes = {\n");
    } else {
      if(isNewPropTypes){
        sb.append("\n\n");
      }
      sb.append(componentName).append(".propTypes = {\n");
    }
    for (int i = 0; i < beans.size(); i++) {
      sb.append(propsBlank).append(beans.get(i).name).append(": PropTypes.");
      if("shape".equals(beans.get(i).type)){
        List<BasePropType> shapePropList = beans.get(i).getShapePropTypeList();
        sb.append("shape");
        if(shapePropList!=null){
          int shapePropSize = shapePropList.size();
          sb.append(shapePropSize > 0 ? "({\n" : "()" );
          for (int j = 0; j <shapePropSize; j++) {
            sb.append(propsBlank).append(PropTypesHelper.getBlank(indent)).append(shapePropList.get(j).name)
                    .append(": PropTypes.").append(shapePropList.get(j).type);
            if (shapePropList.get(j).isRequired) {
              sb.append(".isRequired");
            }
            if (j < shapePropList.size() - 1) sb.append(",\n");
          }
          if(shapePropSize > 0 ){
            sb.append("\n").append(propsBlank).append("})");
          }
        }
      }else {
        sb.append(beans.get(i).type);
      }
      if (beans.get(i).isRequired) {
        sb.append(".isRequired");
      }
      if (i < beans.size() - 1) sb.append(",\n");
    }
    sb.append("\n").append(propsObjBlank).append("}");

    if(!noSemiColons){
      sb.append(";");
    }
    return sb.toString();
  }

  private String getInsertDefaultPropsString(String componentName, List<PropTypeBean> beans,
                                             boolean isNewPropTypes, boolean isES7) {
    int indent = settingService.getState().getIndent();
    boolean noSemiColons = settingService.getState().isNoSemiColons();
    StringBuilder sb = new StringBuilder();
    String propsObjBlank = isES7 ? "  " : "";
    String propsBlank = propsObjBlank + PropTypesHelper.getBlank(indent);
    if (isES7) {
      sb.append("static defaultProps = {\n");
    } else {
      if(isNewPropTypes){
        sb.append("\n\n");
      }
      sb.append(componentName).append(".defaultProps = {\n");
    }
    for (int i = 0; i < beans.size(); i++) {
      sb.append(propsBlank).append(beans.get(i).name).append(": ").append(beans.get(i).getDefaultValue());
      if (i < beans.size() - 1) sb.append(",\n");
    }
    sb.append("\n").append(propsObjBlank).append("}");

    if(!noSemiColons){
      sb.append(";");
    }
    return sb.toString();
  }
}

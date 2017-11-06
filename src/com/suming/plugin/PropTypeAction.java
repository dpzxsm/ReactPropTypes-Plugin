package com.suming.plugin;

import com.intellij.lang.ecmascript6.psi.ES6Class;
import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration;
import com.intellij.lang.javascript.psi.JSAssignmentExpression;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.suming.plugin.bean.PropTypeBean;
import com.suming.plugin.ui.PropTypesDialog;
import com.suming.plugin.utils.PsiElementHelper;

import java.util.List;

public class PropTypeAction extends CommonAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = getEventProject(e);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        PsiFile file = e.getData(PlatformDataKeys.PSI_FILE);
        Caret caret = e.getData(PlatformDataKeys.CARET);
        if (project == null || editor == null || file == null || caret == null) {
            return;
        }

        final String selectedText = getSelectedText(caret);
        if (selectedText == null) {
            showHint(editor, "you must select the text as a Component's name");
            return;
        }

        ES6Class component = getSelectComponent(selectedText, file);
        if (component == null) {
            showHint(editor, "the selected text is not a vaild ES6 Component ");
            return;
        }

        List<String> propNameList = findPropsNameList(component);

        if(propNameList == null || propNameList.size() == 0){
            showHint(editor, "can's find any props");
            return;
        }

        PropTypesDialog dialog = new PropTypesDialog(propNameList);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setOnSubmitListener((beans,isNew) -> {
            Document document = editor.getDocument();
            runCommand(project, () -> {
                //插入PropTypes语句
                insertPropTypesCodeString(document,file,selectedText,beans);
                //自动插入Import语句
                autoInsertImportPropTypes(document,file,isNew);
            });
        });
        dialog.setVisible(true);
    }


    private void autoInsertImportPropTypes(Document document, PsiFile file ,boolean isNew) {
        if(!hasImportPropTypes(isNew,file)){
            int firstImportIndex = findFirstImportIndex(file);
            if(isNew){
                document.insertString(firstImportIndex, "import PropTypes from \'prop-types\'\n");
            }else {
                ES6ImportDeclaration reactImport = getReactImportDeclaration(file);
                if(reactImport == null){
                    document.insertString(firstImportIndex, "import React, {PropTypes} from \'react\'\n");
                }else{
                    if(reactImport.getFromClause() ==null) return;
                    PsiElement pFrom = reactImport.getFromClause();
                    if(pFrom.getPrevSibling()==null || pFrom.getPrevSibling().getPrevSibling()==null) return;
                    PsiElement p1 = pFrom.getPrevSibling();
                    PsiElement p2 = p1.getPrevSibling();
                    PsiElement rbrace = null;
                    if(p1.getText().equals("}")){
                        rbrace = p1;
                    }else if(p2.getText().equals("}")){
                        rbrace = p2;
                    }
                    if(rbrace!=null){
                        int index = rbrace.getTextRange().getStartOffset();
                        boolean isNeedComma = reactImport.getImportSpecifiers().length >0;
                        document.insertString(index, isNeedComma? ",PropTypes": "PropTypes");
                    }else {
                        int index = pFrom.getTextRange().getStartOffset();
                        PsiElement p = PsiElementHelper.getRealPreElement(pFrom);
                        boolean isNeedComma = p!=null&& !p.getText().equals(",");
                        document.insertString(index, isNeedComma? ",{ PropTypes } ": "{ PropTypes }");
                    }
                }

            }
        }
    }


    private void insertPropTypesCodeString(Document document, PsiFile file, String componentName, List<PropTypeBean> beans){
        JSAssignmentExpression expression = getPropTypeElementByName(file,componentName);
        System.out.println("");
        if(expression ==null){
            PsiElement p =  file.getLastChild();
            TextRange pRange = p.getTextRange();
            if(p instanceof PsiWhiteSpace){
                document.replaceString(pRange.getStartOffset(),pRange.getEndOffset(),
                        getInsertPropTypeCodeStringIfNotExist(componentName,beans,true));
            }else {
               document.insertString(pRange.getEndOffset(),
                       getInsertPropTypeCodeStringIfNotExist(componentName,beans,true));
            }
        }else {
            document.replaceString(expression.getTextRange().getStartOffset(), expression.getTextRange().getEndOffset(),
                    getInsertPropTypeCodeStringIfNotExist(componentName,beans,false));
        }
    }

    private String getInsertPropTypeCodeStringIfNotExist(String componentName, List<PropTypeBean> beans ,boolean isNewLine){
        StringBuilder sb = new StringBuilder();
        if(isNewLine)sb.append("\n\n");
        sb.append(componentName).append(".propTypes = {\n");
        for (int i = 0; i < beans.size(); i++) {
            sb.append("    ").append(beans.get(i).name).append(": PropTypes.").append(beans.get(i).type);
            if(beans.get(i).isRequired){
                sb.append(".isRequired");
            }
            if(i< beans.size()-1) sb.append(",\n");
        }
        sb.append("\n}");
        return sb.toString();
    }

}

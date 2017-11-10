package com.suming.plugin;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.lang.ecmascript6.psi.ES6Class;
import com.intellij.lang.ecmascript6.psi.ES6FromClause;
import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.impl.ES6FieldImpl;
import com.intellij.lang.javascript.psi.impl.*;
import com.intellij.lang.javascript.psi.types.JSContext;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.suming.plugin.bean.ESVersion;
import com.suming.plugin.bean.PropTypeBean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

abstract class CommonAction extends AnAction {

  void runCommand(Project project, final Runnable runnable) {
    CommandProcessor.getInstance().executeCommand(project, () -> ApplicationManager.getApplication().runWriteAction(runnable), "inject PropTypes", null);
  }

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
      showHint(editor, "The selected text is not a vaild ES6 Component ");
      return;
    }

    List<PropTypeBean> propNameList = findPropsNameList(component);

    if(propNameList == null || propNameList.size() == 0){
      showHint(editor, "Can's find any props");
      return;
    }

    PsiElement expression = getPropTypeElementByName(file,selectedText);
    if(expression !=null){
      List<PropTypeBean> existPropNameList = findPropsNameListInPropTypeObject(expression);
      for (PropTypeBean anExistPropTypeBean : existPropNameList) {
        for (int j = 0; j < propNameList.size(); j++) {
          if (anExistPropTypeBean.name.equals(propNameList.get(j).name)) {
            propNameList.set(j, anExistPropTypeBean);
            break;
          }
          if(j == propNameList.size() -1){
            propNameList.add(anExistPropTypeBean);
          }
        }
      }
    }
    ESVersion esVersion = expression instanceof ES6FieldImpl? ESVersion.ES7:ESVersion.ES5;
    actionPerformed(project, editor, file, selectedText, propNameList, esVersion);
  }

  abstract void actionPerformed(Project project, Editor editor, PsiFile file, String selectedText,
                                List<PropTypeBean> propNameList, ESVersion esVersion);

  @Nullable
  String getSelectedText(Caret caret) {
    if (caret == null) {
      return null;
    }

    if (caret.getSelectedText() == null || caret.getSelectedText().trim().length() == 0) {
      return null;
    }
    return caret.getSelectedText().trim();
  }

  void showHint(Editor editor, String s) {
    HintManager.getInstance().showErrorHint(editor, s);
  }

  @Nullable
  ES6Class getSelectComponent(String selectText, PsiFile file){
    return PsiTreeUtil.findChildrenOfType(file, JSReferenceExpression.class)
            .stream()
            .filter(o -> o.getText().equals(selectText))
            .filter(o -> o.getParent() instanceof ES6Class)
            .map(o -> (ES6Class)o.getParent())
             .findFirst()
             .orElse(null);
  }

  @Nullable
  boolean hasImportPropTypes(boolean isNew, PsiFile file){
    if(isNew){
      return PsiTreeUtil.findChildrenOfType(file, ES6FromClause.class)
              .stream()
              .filter(o -> o.getText().contains("\'prop-types\'"))
              .map(Objects::nonNull)
              .reduce(false,(a,b) -> a||b);
    }else {
      return PsiTreeUtil.findChildrenOfType(file, ES6FromClause.class)
              .stream()
              .filter(o -> o.getText().contains("\'react\'"))
              .filter(o -> o.getParent().getText().contains("PropTypes"))
              .map(Objects::nonNull)
              .reduce(false,(a,b) -> a||b);
    }
  }

  @Nullable
  ES6ImportDeclaration getReactImportDeclaration(PsiFile file){
    return PsiTreeUtil.findChildrenOfType(file, ES6FromClause.class)
            .stream()
            .filter(o -> o.getText().contains("\'react\'"))
            .filter(o -> o.getParent() instanceof ES6ImportDeclaration)
            .map(o -> (ES6ImportDeclaration) o.getParent())
            .findFirst()
            .orElse(null);

  }

  int findFirstImportIndex(PsiFile file){
    PsiElement[] children = file.getChildren();
    for (PsiElement aChildren : children) {
      if (aChildren instanceof PsiWhiteSpace) {
        continue;
      }
      if (aChildren instanceof PsiComment) {
        continue;
      }
      return aChildren.getTextRange().getStartOffset();
    }
    return 0;
  }

  @Nullable
  List<PropTypeBean> findPropsNameList(PsiElement psiElement) {
    List<String> paramList =  PsiTreeUtil.findChildrenOfType(psiElement, LeafPsiElement.class)
            .stream()
            .filter(o -> o.getText().equals("props"))
            .filter(o -> o.getElementType().toString().equals("JS:IDENTIFIER"))
            .filter(o -> {
              if(o.getParent() instanceof JSReferenceExpressionImpl){
                JSReferenceExpressionImpl parent = (JSReferenceExpressionImpl) o.getParent();
                if(parent.getTreeNext()!=null && parent.getTreeNext().getElementType().toString().equals("JS:DOT")
                        &&parent.getTreeNext().getTreeNext()!=null){
                  return true;
                }
              }
              return  false;
            })
            .map(o -> ((JSReferenceExpressionImpl)o.getParent()).getTreeNext().getTreeNext().getText())
            .distinct()
            .collect(Collectors.toList());

    List<String> destructuringParamList =  PsiTreeUtil.findChildrenOfType(psiElement, LeafPsiElement.class)
            .stream()
            .filter(o -> o.getText().equals("props"))
            .filter(o -> o.getElementType().toString().equals("JS:IDENTIFIER"))
            .filter(o -> {
               if(o.getParent() instanceof  JSReferenceExpressionImpl){
                 JSReferenceExpressionImpl parent = (JSReferenceExpressionImpl) o.getParent();
                 if(parent.getParent() instanceof  JSDestructuringElementImpl
                         && parent.getParent().getFirstChild() instanceof JSDestructuringObjectImpl
                         && parent.getFirstChild() instanceof JSThisExpressionImpl){
                   return true;
                 }
               }
               return  false;
            })
            .map(o -> {
              JSDestructuringElementImpl parent = (JSDestructuringElementImpl) o.getParent().getParent();
              JSDestructuringObject destructuringObject = (JSDestructuringObject) parent.getFirstChild();
              List<String> list = new ArrayList<>();
              PsiElement[] elements = destructuringObject.getChildren();
              for (PsiElement element : elements) {
                if (element instanceof JSDestructuringShorthandedProperty) {
                  JSDestructuringShorthandedPropertyImpl property = (JSDestructuringShorthandedPropertyImpl) element;
                  JSVariable variable = property.getDestructuringElement();
                  if (variable != null) list.add(variable.getText());
                }
              }
              return list;
            })
            .reduce(new ArrayList<>(),(a, b)-> {
              a.addAll(b);
              return a;
            });
    paramList.addAll(destructuringParamList);
    return paramList.stream()
            .distinct()
            .sorted()
            .map(o -> new PropTypeBean(o,"any", "false"))
            .collect(Collectors.toList());
  }

  @NotNull
  List<PropTypeBean> findPropsNameListInPropTypeObject(PsiElement expression){
    List<PropTypeBean> paramList = new ArrayList<>();
    if(expression.getLastChild() != null &&  expression.getLastChild() instanceof JSObjectLiteralExpression){
      JSObjectLiteralExpression literalExpression = (JSObjectLiteralExpression) expression.getLastChild();
      JSProperty[] properties = literalExpression.getProperties();
      for (JSProperty property : properties) {
        if(property.getLastChild().getText().contains("PropTypes")){
          Pattern p = Pattern.compile("PropTypes\\s*\\.\\s*(any|string|object|bool|func|number|array|symbol)\\s*\\.?\\s*(isRequired)?");
          Matcher m = p.matcher(property.getLastChild().getText());
          if(m.matches()){
            String type = m.group(1)==null?"any":m.group(1);
            String isRequired = m.group(2)==null?"false":"true";
            paramList.add(new PropTypeBean(property.getName(),type,isRequired));
          }
        }
      }
    }
    return  paramList;
  }

  @Nullable
  PsiElement getES7PropTypeElementByName(PsiFile file, String componentName){
    ES6Class es6Class = getSelectComponent(componentName,file);
    if(es6Class == null) return  null;
    return  PsiTreeUtil.findChildrenOfType(es6Class, ES6FieldImpl.class)
            .stream()
            .filter(o -> Objects.equals(o.getName(), "propTypes"))
            .filter(o -> o.getJSContext() == JSContext.STATIC )
            .findFirst()
            .orElse(null);
  }

  PsiElement getES6PropTypeElementByName(PsiFile file, String componentName){
    return PsiTreeUtil.findChildrenOfType(file, JSDefinitionExpression.class)
            .stream()
            .filter(o -> o.getText().matches(componentName + "\\s*\\.\\s*propTypes"))
            .filter(o -> o.getParent() instanceof JSAssignmentExpression)
            .map(o -> (JSAssignmentExpression) o.getParent())
            .filter(o -> o.getLastChild() instanceof JSObjectLiteralExpression)
            .findFirst()
            .orElse(null);
  }

  @Nullable
  PsiElement getPropTypeElementByName (PsiFile file, String componentName){
    PsiElement psiElement = getES7PropTypeElementByName(file, componentName);
    if(psiElement == null) {
      return getES6PropTypeElementByName(file,componentName);
    }else {
      return psiElement;
    }
  }
}

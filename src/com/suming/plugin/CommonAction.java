package com.suming.plugin;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.lang.ecmascript6.psi.ES6Class;
import com.intellij.lang.ecmascript6.psi.ES6ClassExpression;
import com.intellij.lang.ecmascript6.psi.ES6FromClause;
import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration;
import com.intellij.lang.ecmascript6.psi.impl.ES6ClassExpressionImpl;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.impl.*;
import com.intellij.openapi.actionSystem.AnAction;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

abstract class CommonAction extends AnAction {

  void runCommand(Project project, final Runnable runnable) {
    CommandProcessor.getInstance().executeCommand(project, () -> ApplicationManager.getApplication().runWriteAction(runnable), "inject PropTypes", null);
  }

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

  boolean getComponentPropTypes(String ComponentName, PsiFile file){
    return false;
  }

  @Nullable
  List<String> findPropsNameList(PsiElement psiElement) {
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
                  if (!property.getFirstChild().getText().equals("...")) {
                    JSVariable variable = property.getDestructuringElement();
                    if (variable != null) list.add(variable.getText());
                  }
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
            .collect(Collectors.toList());
  }


  @Nullable
  JSAssignmentExpression getPropTypeElementByName (PsiFile file, String componentName){
    return  PsiTreeUtil.findChildrenOfType(file, JSDefinitionExpression.class)
            .stream()
            .filter(o -> o.getText().matches(componentName+"\\s*\\.\\s*propTypes"))
            .filter(o -> o.getParent() instanceof JSAssignmentExpression)
            .map(o -> (JSAssignmentExpression)o.getParent())
            .findFirst()
            .orElse(null);
  }
}

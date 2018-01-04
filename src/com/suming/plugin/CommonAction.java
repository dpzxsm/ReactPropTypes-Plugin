package com.suming.plugin;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.lang.ecmascript6.psi.ES6Class;
import com.intellij.lang.ecmascript6.psi.ES6FromClause;
import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.impl.ES6FieldImpl;
import com.intellij.lang.javascript.psi.impl.JSDestructuringElementImpl;
import com.intellij.lang.javascript.psi.impl.JSDestructuringObjectImpl;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.lang.javascript.psi.types.JSContext;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlElement;
import com.suming.plugin.bean.*;
import com.suming.plugin.utils.PropTypesHelper;
import com.suming.plugin.utils.SelectWordUtilCompat;
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

    final String selectedText = getSelectedText(editor);
    if (selectedText == null) {
      showHint(editor, "you must select the text as a Component's name");
      return;
    }

    Component component = getSelectComponent(selectedText, file);
    if (component == null) {
      showHint(editor, "The selected text is not a valid React Component ");
      return;
    }
    // create a empty list
    List<PropTypeBean> newPropNameList = new ArrayList<>();
    List<PropTypeBean> propNameList = new ArrayList<>();
    // find all use propTypes
    propNameList.addAll(findPropsNameList(component));
    // find default propTypes
    List<DefaultPropType> defaultPropTypeList = findPropsNameListInDefaultPropsElement(getDefaultPropsElementByName(file,selectedText));
    for (DefaultPropType defaultPropType : defaultPropTypeList){
      for (int i = 0; i < propNameList.size(); i++) {
        if(propNameList.get(i).name.equals(defaultPropType.name)){
          propNameList.get(i).type = defaultPropType.type;
          break;
        }
        if(i == propNameList.size() -1){
          propNameList.add(new PropTypeBean(defaultPropType.name, defaultPropType.type, false));
        }
      }
    }
    // filter exist list
    PsiElement expression = getPropTypeElementByName(file,selectedText);
    if(expression !=null){
      List<PropTypeBean> existPropNameList = findPropsNameListInPropTypeObject(expression);
      for (PropTypeBean propTypeBean : propNameList){
        for (int i = 0; i < existPropNameList.size(); i++) {
          if(existPropNameList.get(i).name.equals(propTypeBean.name)){
            break;
          }
          if(i == existPropNameList.size() -1){
            propTypeBean.setDescribe("new added");
            newPropNameList.add(propTypeBean);
          }
        }
      }
      for (PropTypeBean propTypeBean : existPropNameList){
        for (int i = 0; i < propNameList.size(); i++) {
          if(propNameList.get(i).name.equals(propTypeBean.name)){
            break;
          }
          if(i == propNameList.size() -1){
            propTypeBean.setDescribe("never used");
          }
        }
      }
      newPropNameList.addAll(existPropNameList);
      if(expression instanceof ES6FieldImpl){
        component.setEsVersion(ESVersion.ES7);
      }else {
        component.setEsVersion(ESVersion.ES6);
      }
    }else {
      newPropNameList.addAll(propNameList);
    }

    actionPerformed(project, editor, file, selectedText, newPropNameList, component);
  }

  abstract void actionPerformed(Project project, Editor editor, PsiFile file, String selectedText,
                                List<PropTypeBean> propNameList, Component component);

  @Nullable
  String getSelectedText(Editor editor) {
    SelectionModel selectionModel = editor.getSelectionModel();
    if(selectionModel.hasSelection()){
      return selectionModel.getSelectedText();
    }else {
      final ArrayList<TextRange> ranges = new ArrayList<>();
      final int offset = editor.getCaretModel().getOffset();
      SelectWordUtilCompat.addWordOrLexemeSelection(false, editor, offset, ranges, SelectWordUtilCompat.JAVASCRIPT_IDENTIFIER_PART_CONDITION);
      if(ranges.size()>0){
        return  editor.getDocument().getText(ranges.get(0));
      }else {
        return null;
      }
    }
  }

  void showHint(Editor editor, String s) {
    HintManager.getInstance().showErrorHint(editor, s);
  }

  @Nullable
  ES6Class getSelectES6Component(String selectText, PsiFile file){
    return PsiTreeUtil.findChildrenOfType(file, JSReferenceExpression.class)
            .stream()
            .filter(o -> o.getText().equals(selectText))
            .filter(o -> o.getParent() instanceof ES6Class)
            .map(o -> (ES6Class)o.getParent())
            .findFirst()
            .orElse(null);
  }

  @Nullable
  private JSFunction getSelectStatelessComponent(String selectText, PsiFile file){
    return PsiTreeUtil.findChildrenOfType(file, JSFunction.class)
            .stream()
            .filter(o -> o.getName()!=null && o.getName().equals(selectText))
            .filter(o -> {
              XmlElement element = PsiTreeUtil.findChildrenOfType(o, XmlElement.class)
                      .stream()
                      .findFirst()
                      .orElse(null);
              return  element != null;
            })
            .findFirst()
            .orElse(null);

  }

  @Nullable
  private Component getSelectComponent(String selectText, PsiFile file){
    ES6Class es6Class = getSelectES6Component(selectText,file);
    if(es6Class != null){
      return new Component(es6Class, ComponentType.STANDARD, ESVersion.ES6);
    }else {
      JSFunction statelessElement = getSelectStatelessComponent(selectText, file);
      if(statelessElement!=null){
        return new Component(statelessElement, ComponentType.STATELESS);
      }else {
        return null;
      }
    }
  }

  @Nullable
  boolean hasImportPropTypes(PsiFile file){
    boolean hasNew = PsiTreeUtil.findChildrenOfType(file, ES6FromClause.class)
            .stream()
            .filter(o -> o.getText().contains("\'prop-types\'"))
            .map(Objects::nonNull)
            .reduce(false,(a,b) -> a||b);
    boolean hasOld = PsiTreeUtil.findChildrenOfType(file, ES6FromClause.class)
            .stream()
            .filter(o -> o.getText().contains("\'react\'"))
            .filter(o -> o.getParent().getText().contains("PropTypes"))
            .map(Objects::nonNull)
            .reduce(false,(a,b) -> a||b);
    return hasNew||hasOld;
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

  @NotNull
  private List<PropTypeBean> findPropsNameList(Component component) {
    PsiElement psiElement = component.getElement();
    ComponentType componentType = component.getComponentType();
    List<PropTypeBean> paramList = new ArrayList<>();
    if(componentType == ComponentType.STATELESS){
      JSParameterList jsParameterList = ((JSFunction) psiElement).getParameterList();
      JSParameterListElement propsParam = (jsParameterList != null && jsParameterList.getParameters().length>0)?
              jsParameterList.getParameters()[0]: null;
      if(propsParam != null){
        if(propsParam instanceof JSDestructuringParameter){
          JSDestructuringElement parent = (JSDestructuringElement) propsParam;
          JSDestructuringObject destructuringObject = (JSDestructuringObject) parent.getFirstChild();
          paramList.addAll(getPropsWithDestructuringProperty(destructuringObject));
        }else {
          paramList.addAll(findPropsNameListByPropsIdentity(propsParam.getName(),psiElement));
        }
      }
    }else {
      paramList.addAll(findPropsNameListByPropsIdentity("props",psiElement));
    }
    // maybe have duplicate data, so must distinct
    return paramList.stream()
            .sorted((o1, o2) -> {
              if(o1.name.equals(o2.name)){
                if(o1.type.equals(o2.type)){
                  return  0;
                }else if(o2.type.equals("any")){
                  return -1;
                }else {
                  return  1;
                }
              }else {
               return o1.name.compareTo(o2.name);
              }
            })
            .filter(PropTypesHelper.distinctByKey(PropTypeBean::getName))
            .collect(Collectors.toList());
  }


  @NotNull
  private List<PropTypeBean> getPropsWithDestructuringProperty(JSDestructuringObject destructuringObject){
    List<PropTypeBean> propTypeBeans = new ArrayList<>();
    PsiElement[] elements = destructuringObject.getChildren();
    for (PsiElement element : elements) {
      if (element instanceof JSDestructuringProperty) {
        JSDestructuringProperty property = (JSDestructuringProperty) element;
        JSInitializerOwner owner = property.getDestructuringElement();
        JSExpression initializer = owner!=null?owner.getInitializer():null ;
        String type = "any";
        if(initializer != null){
          type = PropTypesHelper.getPropTypeByValue(initializer.getText());
        }else if(owner != null){
          String ownerStr = owner.getText();
          if(ownerStr.startsWith("{") && ownerStr.endsWith("}")){
            type = "object";
          }
        }
        propTypeBeans.add(new PropTypeBean(property.getName(), type, false));
      }
    }
    return propTypeBeans;
  }

  @NotNull
  private List<PropTypeBean>  findPropsNameListByPropsIdentity(String identity, PsiElement psiElement){
    // maybe contains default value
    List<PropTypeBean> destructuringParamList =  PsiTreeUtil.findChildrenOfType(psiElement, LeafPsiElement.class)
            .stream()
            .filter(o -> o.getText().equals(identity))
            .filter(o -> o.getElementType().toString().equals("JS:IDENTIFIER"))
            .filter(o -> {
              if(o.getParent() instanceof  JSReferenceExpressionImpl){
                JSReferenceExpressionImpl parent = (JSReferenceExpressionImpl) o.getParent();
                if(parent.getParent() instanceof  JSDestructuringElementImpl
                        && parent.getParent().getFirstChild() instanceof JSDestructuringObjectImpl){
                  return true;
                }
              }
              return  false;
            })
            .map(o -> {
              JSDestructuringElementImpl parent = (JSDestructuringElementImpl) o.getParent().getParent();
              JSDestructuringObject destructuringObject = (JSDestructuringObject) parent.getFirstChild();
              return getPropsWithDestructuringProperty(destructuringObject);
            })
            .reduce(new ArrayList<>(),(a, b)-> {
              a.addAll(b);
              return a;
            });

    // must not have default value
    List<PropTypeBean> paramList =  PsiTreeUtil.findChildrenOfType(psiElement, LeafPsiElement.class)
            .stream()
            .filter(o -> o.getText().equals(identity))
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
            .map(o -> new PropTypeBean(o,"any", false))
            .collect(Collectors.toList());
    paramList.addAll(destructuringParamList);
    return paramList;
  }

  @Nullable
  PsiElement getES7FieldElementByName(PsiFile file, String componentName , String fieldName ){
    ES6Class es6Class = getSelectES6Component(componentName,file);
    if(es6Class == null) return  null;
    return  PsiTreeUtil.findChildrenOfType(es6Class, ES6FieldImpl.class)
            .stream()
            .filter(o -> Objects.equals(o.getName(), fieldName))
            .filter(o -> o.getJSContext() == JSContext.STATIC )
            .findFirst()
            .orElse(null);
  }

  @Nullable
  PsiElement getES6FieldByName(PsiFile file, String componentName , String fieldName){
    return PsiTreeUtil.findChildrenOfType(file, JSDefinitionExpression.class)
            .stream()
            .filter(o -> o.getText().matches(componentName + "\\s*\\.\\s*" + fieldName))
            .filter(o -> o.getParent() instanceof JSAssignmentExpression)
            .map(o -> (JSAssignmentExpression) o.getParent())
            .filter(o -> o.getLastChild() instanceof JSObjectLiteralExpression)
            .findFirst()
            .orElse(null);
  }

  @Nullable
  private PsiElement getES5PropTypeElementByName(PsiFile file, String componentName){
    return null;
  }

  @NotNull
  List<DefaultPropType> findPropsNameListInDefaultPropsElement(PsiElement expression){
    List<DefaultPropType> paramList = new ArrayList<>();
    if(expression.getLastChild() != null &&  expression.getLastChild() instanceof JSObjectLiteralExpression){
      JSObjectLiteralExpression literalExpression = (JSObjectLiteralExpression) expression.getLastChild();
      JSProperty[] properties = literalExpression.getProperties();
      for (JSProperty property : properties) {
        String name = property.getName();
        String value = property.getValue()!=null?property.getValue().getText():"";
        DefaultPropType defaultPropType = new DefaultPropType(name, value,PropTypesHelper.getPropTypeByValue(value));
        paramList.add(defaultPropType);
      }
    }
    return  paramList;
  }

  @Nullable
  private PsiElement getDefaultPropsElementByName(PsiFile file, String componentName){
    PsiElement es7Element = getES7FieldElementByName(file, componentName, "defaultProps");
    if(es7Element == null) {
      PsiElement es6Element = getES6FieldByName(file,componentName , "defaultProps");
      if(es6Element == null){
        return getES5PropTypeElementByName(file,componentName);
      }else {
        return es6Element;
      }
    }else {
      return es7Element;
    }
  }

  @NotNull
  List<PropTypeBean> findPropsNameListInPropTypeObject(PsiElement expression){
    List<PropTypeBean> paramList = new ArrayList<>();
    if(expression.getLastChild() != null &&  expression.getLastChild() instanceof JSObjectLiteralExpression){
      JSObjectLiteralExpression literalExpression = (JSObjectLiteralExpression) expression.getLastChild();
      JSProperty[] properties = literalExpression.getProperties();
      for (JSProperty property : properties) {
        if(property.getLastChild().getText().contains("PropTypes")){
          Pattern p = Pattern.compile("(React)?\\s*\\.?\\s*PropTypes\\s*\\.\\s*(any|string|object|bool|func|number|array|symbol)\\s*\\.?\\s*(isRequired)?");
          Matcher m = p.matcher(property.getLastChild().getText());
          if(m.matches()){
            String type = m.group(2)==null?"any":m.group(2);
            boolean isRequired = m.group(3) != null;
            paramList.add(new PropTypeBean(property.getName(),type, isRequired));
          }
        }
      }
    }
    return  paramList;
  }

  @Nullable
  private PsiElement getPropTypeElementByName(PsiFile file, String componentName){
    // ES7 is ES6Field , ES6 is JSDefinitionExpression, ES5 is JSField
    PsiElement es7Element = getES7FieldElementByName(file, componentName, "propTypes");
    if(es7Element == null) {
      PsiElement es6Element = getES6FieldByName(file,componentName , "propTypes");
      if(es6Element == null){
        return getES5PropTypeElementByName(file,componentName);
      }else {
        return es6Element;
      }
    }else {
      return es7Element;
    }
  }
}

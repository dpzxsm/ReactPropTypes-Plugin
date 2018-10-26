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
import com.intellij.openapi.components.ServiceManager;
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
import com.suming.plugin.bean.Component;
import com.suming.plugin.bean.ComponentType;
import com.suming.plugin.bean.ESVersion;
import com.suming.plugin.bean.PropTypeBean;
import com.suming.plugin.constants.ArrayFunctions;
import com.suming.plugin.persist.SettingService;
import com.suming.plugin.utils.PropTypesHelper;
import com.suming.plugin.utils.SelectWordUtilCompat;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

abstract class CommonAction extends AnAction {
    SettingService settingService = ServiceManager.getService(SettingService.class);

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
        // find all use propTypes
        List<PropTypeBean> usePropNameList = findPropsNameList(component);

        // add to new list
        newPropNameList.addAll(usePropNameList);

        // filter exist list
        PsiElement expression = getPropTypeElementByName(file, selectedText);
        if (expression != null) {
            List<PropTypeBean> existPropNameList = findPropsNameListInPropTypeObject(expression);
            if (usePropNameList.size() == 0) {
                newPropNameList.addAll(existPropNameList);
            } else {
                for (PropTypeBean propTypeBean : existPropNameList) {
                    for (int i = 0; i < usePropNameList.size(); i++) {
                        PropTypeBean usePropType = usePropNameList.get(i);
                        if (usePropType.name.equals(propTypeBean.name)) {
                            if (TextUtils.isEmpty(usePropType.type) || usePropType.type.equals("any")) {
                                usePropType.setType(propTypeBean.type);
                            }
                            usePropType.setRequired(propTypeBean.isRequired);
                            usePropType.setShapePropTypeList(propTypeBean.getShapePropTypeList());
                            break;
                        }
                        if (i == usePropNameList.size() - 1) {
                            newPropNameList.add(0, propTypeBean);
                        }
                    }
                }
            }
            if (expression instanceof ES6FieldImpl) {
                component.setEsVersion(ESVersion.ES7);
            } else {
                component.setEsVersion(ESVersion.ES6);
            }
        }

        // filter default propTypes
        List<PropTypeBean> defaultPropTypeList = findPropsNameListInDefaultPropsElement(getDefaultPropsElementByName(file, selectedText));
        for (PropTypeBean defaultPropType : defaultPropTypeList) {
            for (int i = 0; i < newPropNameList.size(); i++) {
                if (newPropNameList.get(i).name.equals(defaultPropType.name)) {
                    PropTypeBean newProp = newPropNameList.get(i);
                    if (TextUtils.isEmpty(newProp.type) || newProp.type.equals("any")) {
                        newPropNameList.get(i).setType(defaultPropType.type);
                    }
                    newPropNameList.get(i).setDefaultValue(defaultPropType.getDefaultValue());
                    break;
                }
                if (i == newPropNameList.size() - 1) {
                    PropTypeBean bean = new PropTypeBean(defaultPropType.name, defaultPropType.type,
                            false, defaultPropType.getDefaultValue());
                    newPropNameList.add(bean);
                }
            }
        }

        actionPerformed(project, editor, file, selectedText, newPropNameList, component);
    }

    abstract void actionPerformed(Project project, Editor editor, PsiFile file, String selectedText,
                                  List<PropTypeBean> propNameList, Component component);

    @Nullable
    String getSelectedText(Editor editor) {
        SelectionModel selectionModel = editor.getSelectionModel();
        if (selectionModel.hasSelection()) {
            return selectionModel.getSelectedText();
        } else {
            final ArrayList<TextRange> ranges = new ArrayList<>();
            final int offset = editor.getCaretModel().getOffset();
            SelectWordUtilCompat.addWordOrLexemeSelection(false, editor, offset, ranges, SelectWordUtilCompat.JAVASCRIPT_IDENTIFIER_PART_CONDITION);
            if (ranges.size() > 0) {
                return editor.getDocument().getText(ranges.get(0));
            } else {
                return null;
            }
        }
    }

    void showHint(Editor editor, String s) {
        HintManager.getInstance().showErrorHint(editor, s);
    }

    @Nullable
    ES6Class getSelectES6Component(String selectText, PsiFile file) {
        return PsiTreeUtil.findChildrenOfType(file, JSReferenceExpression.class)
                .stream()
                .filter(o -> o.getText().equals(selectText))
                .filter(o -> o.getParent() instanceof ES6Class)
                .map(o -> (ES6Class) o.getParent())
                .findFirst()
                .orElse(null);
    }

    @Nullable
    private JSFunction getSelectStatelessComponent(String selectText, PsiFile file) {
        return PsiTreeUtil.findChildrenOfType(file, JSFunction.class)
                .stream()
                .filter(o -> o.getName() != null && o.getName().equals(selectText))
                .filter(o -> {
                    if (this.settingService.getState().isUncheckFunctionalComponent()) {
                        return true;
                    } else {
                        XmlElement element = PsiTreeUtil.findChildrenOfType(o, XmlElement.class)
                                .stream()
                                .findFirst()
                                .orElse(null);
                        return element != null;
                    }
                })
                .findFirst()
                .orElse(null);

    }

    @Nullable
    private Component getSelectComponent(String selectText, PsiFile file) {
        ES6Class es6Class = getSelectES6Component(selectText, file);
        if (es6Class != null) {
            return new Component(es6Class, ComponentType.STANDARD);
        } else {
            JSFunction statelessElement = getSelectStatelessComponent(selectText, file);
            if (statelessElement != null) {
                return new Component(statelessElement, ComponentType.STATELESS, ESVersion.ES6);
            } else {
                return null;
            }
        }
    }

    @Nullable
    boolean hasImportPropTypes(PsiFile file) {
        boolean hasNew = PsiTreeUtil.findChildrenOfType(file, ES6FromClause.class)
                .stream()
                .filter(o -> o.getText().contains("\'prop-types\'"))
                .map(Objects::nonNull)
                .reduce(false, (a, b) -> a || b);
        boolean hasOld = PsiTreeUtil.findChildrenOfType(file, ES6FromClause.class)
                .stream()
                .filter(o -> o.getText().contains("\'react\'"))
                .filter(o -> o.getParent().getText().contains("PropTypes"))
                .map(Objects::nonNull)
                .reduce(false, (a, b) -> a || b);
        return hasNew || hasOld;
    }

    @Nullable
    ES6ImportDeclaration getReactImportDeclaration(PsiFile file) {
        return PsiTreeUtil.findChildrenOfType(file, ES6FromClause.class)
                .stream()
                .filter(o -> o.getText().contains("\'react\'"))
                .filter(o -> o.getParent() instanceof ES6ImportDeclaration)
                .map(o -> (ES6ImportDeclaration) o.getParent())
                .findFirst()
                .orElse(null);

    }

    int findFirstImportIndex(PsiFile file) {
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
        if (componentType == ComponentType.STATELESS) {
            JSParameterList jsParameterList = ((JSFunction) psiElement).getParameterList();
            JSParameterListElement propsParam = (jsParameterList != null && jsParameterList.getParameters().length > 0) ?
                    jsParameterList.getParameters()[0] : null;
            if (propsParam != null) {
                if (propsParam instanceof JSDestructuringParameter) {
                    JSDestructuringElement parent = (JSDestructuringElement) propsParam;
                    JSDestructuringObject destructuringObject = (JSDestructuringObject) parent.getFirstChild();
                    paramList.addAll(getPropsWithDestructuringProperty(destructuringObject, parent.getParent().getParent()));
                } else {
                    paramList.addAll(findPropsNameListByPropsIdentity(propsParam.getName(), psiElement));
                }
            }
        } else if (componentType == ComponentType.STANDARD) {
            paramList.addAll(findPropsNameListByPropsIdentity("props", psiElement));
            paramList.addAll(findPropsNameListByPropsIdentity("nextProps", psiElement));
        } else {
            // ES5 is not supported for the time being
        }
        // maybe have duplicate data, so must distinct
        HashSet<Integer> reverseSet = new HashSet<>();
        reverseSet.add(1);
        reverseSet.add(3);
        return paramList.stream()
                .sorted(PropTypesHelper.sortByKey(reverseSet, PropTypeBean::getName, PropTypeBean::getType,
                        PropTypeBean::getDefaultValue, PropTypeBean::isRequired))
                .filter(PropTypesHelper.distinctByKey(PropTypeBean::getName))
                .collect(Collectors.toList());
    }


    @NotNull
    private List<PropTypeBean> getPropsWithDestructuringProperty(JSDestructuringObject destructuringObject,
                                                                 PsiElement parentElement) {
        List<PropTypeBean> propTypeBeans = new ArrayList<>();
        PsiElement[] elements = destructuringObject.getChildren();
        for (PsiElement element : elements) {
            if (element instanceof JSDestructuringProperty) {
                JSDestructuringProperty property = (JSDestructuringProperty) element;
                JSInitializerOwner owner = property.getDestructuringElement();
                PsiElement firstChild = property.getFirstChild();
                JSExpression initializer = owner != null ? owner.getInitializer() : null;
                if (firstChild.getText().equals("...")) {
                    // prevent loop
                    propTypeBeans.addAll(findPropsNameListWithIdentityReference(property.getName(), parentElement));
                } else if (firstChild instanceof JSVariable) {
                    PropTypeBean bean = new PropTypeBean(property.getName());
                    // only setting's inferByDestructure  equal true
                    if (settingService.getState().isInferByDestructure()) {
                        if (initializer != null) {
                            bean.setType(PropTypesHelper.getPropTypeByValue(initializer.getText()));
                            bean.setDefaultValue(initializer.getText());
                        } else if (owner != null) {
                            String ownerStr = owner.getText();
                            if (ownerStr.startsWith("{") && ownerStr.endsWith("}")) {
                                bean.setType("object");
                            }
                        }
                    }
                    propTypeBeans.add(findPropsNameTypeAndSetType(bean, null, parentElement));
                }
            }
        }
        return propTypeBeans;
    }

    @NotNull
    private List<PropTypeBean> findPropsNameListByPropsIdentity(String identity, PsiElement psiElement) {
        // maybe contains default value, so find first
        List<PropTypeBean> destructuringParamList = PsiTreeUtil.findChildrenOfType(psiElement, LeafPsiElement.class)
                .stream()
                .filter(o -> o.getText().equals(identity))
                .filter(o -> o.getElementType().toString().equals("JS:IDENTIFIER"))
                .filter(o -> {
                    if (o.getParent() instanceof JSReferenceExpressionImpl) {
                        JSReferenceExpressionImpl parent = (JSReferenceExpressionImpl) o.getParent();
                        if (parent.getParent() instanceof JSDestructuringElementImpl
                                && parent.getParent().getFirstChild() instanceof JSDestructuringObjectImpl
                                && parent.getParent().getParent() != null) {
                            return true;
                        }
                    }
                    return false;
                })
                .map(o -> {
                    JSDestructuringElementImpl parent = (JSDestructuringElementImpl) o.getParent().getParent();
                    JSDestructuringObject destructuringObject = (JSDestructuringObject) parent.getFirstChild();
                    return getPropsWithDestructuringProperty(destructuringObject, parent.getParent().getParent());
                })
                .reduce(new ArrayList<>(), (a, b) -> {
                    a.addAll(b);
                    return a;
                });
        List<PropTypeBean> paramList = findPropsNameListWithIdentityReference(identity, psiElement);
        paramList.addAll(destructuringParamList);
        return paramList;
    }

    @NotNull
    private PropTypeBean findPropsNameTypeAndSetType(PropTypeBean bean, @Nullable String identity, PsiElement psiElement) {
        // disable infer type
        if (!settingService.getState().isInferByPropsCall()) return bean;

        String firstReg = identity != null ? ".*\\s*" + identity + "\\s*\\.\\s*" : "";
        String funcReg = firstReg + bean.name + "\\s*\\(" + ".*";

        boolean isFunc = PsiTreeUtil.findChildrenOfType(psiElement, JSCallExpression.class)
                .stream()
                .anyMatch(o -> o.getText().matches(funcReg));
        // only infer func and array
        if (isFunc) {
            bean.setType("func");
        } else {
            String arrayFuncString = Arrays.stream(ArrayFunctions.values())
                    .map(o -> (o.name()))
                    .reduce("", (a, b) -> a + '|' + b);
            String arrayReg = firstReg + bean.name + "\\s*\\.\\s*" + arrayFuncString.replaceFirst("^\\|", "(") + ")" + ".*";
            String objectReq = firstReg + bean.name + "\\s*\\.\\s*.*";
            boolean isObject = PsiTreeUtil.findChildrenOfType(psiElement, JSCallExpression.class)
                    .stream()
                    .anyMatch(o -> o.getText().matches(objectReq));
            if (isObject) {
                boolean isArray = PsiTreeUtil.findChildrenOfType(psiElement, JSCallExpression.class)
                        .stream()
                        .anyMatch(o -> o.getText().matches(arrayReg));
                if (isArray) {
                    bean.setType("array");
                } else {
                    bean.setType("object");
                }
            }
        }
        return bean;
    }

    @NotNull
    private List<PropTypeBean> findPropsNameListWithIdentityReference(String identity, PsiElement psiElement) {
        return PsiTreeUtil.findChildrenOfType(psiElement, LeafPsiElement.class)
                .stream()
                .filter(o -> o.getText().equals(identity))
                .filter(o -> o.getElementType().toString().equals("JS:IDENTIFIER"))
                .filter(o -> {
                    if (o.getParent() instanceof JSReferenceExpressionImpl) {
                        JSReferenceExpressionImpl parent = (JSReferenceExpressionImpl) o.getParent();
                        if (parent.getTreeNext() != null && parent.getTreeNext().getElementType().toString().equals("JS:DOT")
                                && parent.getTreeNext().getTreeNext() != null) {
                            return true;
                        }
                    }
                    return false;
                })
                .map(o -> ((JSReferenceExpressionImpl) o.getParent()).getTreeNext().getTreeNext().getText())
                .distinct()
                .map(o -> findPropsNameTypeAndSetType(new PropTypeBean(o, "any", false), identity, psiElement))
                .collect(Collectors.toList());
    }


    @Nullable
    PsiElement getES7FieldElementByName(PsiFile file, String componentName, String fieldName) {
        ES6Class es6Class = getSelectES6Component(componentName, file);
        if (es6Class == null) return null;
        return PsiTreeUtil.findChildrenOfType(es6Class, ES6FieldImpl.class)
                .stream()
                .filter(o -> Objects.equals(o.getName(), fieldName))
                .filter(o -> o.getJSContext() == JSContext.STATIC)
                .findFirst()
                .orElse(null);
    }

    @Nullable
    PsiElement getES6FieldByName(PsiFile file, String componentName, String fieldName) {
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
    private PsiElement getES5PropTypeElementByName(PsiFile file, String componentName) {
        return null;
    }

    /**
     * Find defaultProps Object and to PropTypeBean List
     *
     * @param expression
     * @return
     */
    @NotNull
    List<PropTypeBean> findPropsNameListInDefaultPropsElement(PsiElement expression) {
        List<PropTypeBean> paramList = new ArrayList<>();
        if (expression != null && expression.getLastChild() != null && expression.getLastChild() instanceof JSObjectLiteralExpression) {
            JSObjectLiteralExpression literalExpression = (JSObjectLiteralExpression) expression.getLastChild();
            JSProperty[] properties = literalExpression.getProperties();
            for (JSProperty property : properties) {
                String name = property.getName();
                String value = property.getValue() != null ? property.getValue().getText() : "";
                boolean inferByDefaultProps = settingService.getState().isInferByDefaultProps();
                PropTypeBean bean = new PropTypeBean(name, inferByDefaultProps ? PropTypesHelper.getPropTypeByValue(value) : "any", false);
                bean.setDefaultValue(value);
                paramList.add(bean);
            }
        }
        return paramList;
    }

    /**
     * Find defaultProps Object from a JS File with componentName
     *
     * @param file
     * @param componentName
     * @return
     */
    @Nullable
    private PsiElement getDefaultPropsElementByName(PsiFile file, String componentName) {
        PsiElement es7Element = getES7FieldElementByName(file, componentName, "defaultProps");
        if (es7Element == null) {
            PsiElement es6Element = getES6FieldByName(file, componentName, "defaultProps");
            if (es6Element == null) {
                return getES5PropTypeElementByName(file, componentName);
            } else {
                return es6Element;
            }
        } else {
            return es7Element;
        }
    }

    /**
     * Find propTypes Object and to PropTypeBean List
     *
     * @param expression
     * @return
     */
    @NotNull
    List<PropTypeBean> findPropsNameListInPropTypeObject(PsiElement expression) {
        List<PropTypeBean> paramList = new ArrayList<>();
        if (expression != null && expression.getLastChild() != null && expression.getLastChild() instanceof JSObjectLiteralExpression) {
            JSObjectLiteralExpression literalExpression = (JSObjectLiteralExpression) expression.getLastChild();
            JSProperty[] properties = literalExpression.getProperties();
            for (JSProperty property : properties) {
                if (property.getName() != null && property.getLastChild().getText().contains("PropTypes")) {
                    PropTypeBean bean = new PropTypeBean(property.getName());
                    PropTypesHelper.updatePropTypeFromCode(bean, property.getLastChild().getText());
                    paramList.add(bean);
                }

            }
        }
        return paramList;
    }

    /**
     * Find propTypes Object from a JS File with componentName
     *
     * @param file
     * @param componentName
     * @return
     */
    @Nullable
    private PsiElement getPropTypeElementByName(PsiFile file, String componentName) {
        // ES7 is ES6Field , ES6 is JSDefinitionExpression, ES5 is JSField
        PsiElement es7Element = getES7FieldElementByName(file, componentName, "propTypes");
        if (es7Element == null) {
            PsiElement es6Element = getES6FieldByName(file, componentName, "propTypes");
            if (es6Element == null) {
                return getES5PropTypeElementByName(file, componentName);
            } else {
                return es6Element;
            }
        } else {
            return es7Element;
        }
    }
}

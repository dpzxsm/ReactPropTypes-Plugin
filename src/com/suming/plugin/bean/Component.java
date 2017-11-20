package com.suming.plugin.bean;

import com.intellij.psi.PsiElement;

public class Component {
    private PsiElement element;
    private ComponentType componentType;
    private ESVersion esVersion;

    public Component(PsiElement element, ComponentType componentType ) {
        this.element = element;
        this.componentType = componentType;
    }

    public Component(PsiElement element, ComponentType componentType, ESVersion esVersion) {
        this.element = element;
        this.componentType = componentType;
        this.esVersion = esVersion;
    }

    public PsiElement getElement() {
        return element;
    }

    public ComponentType getComponentType() {
        return componentType;
    }

    public ESVersion getEsVersion() {
        return esVersion;
    }

    public void setElement(PsiElement element) {
        this.element = element;
    }

    public void setComponentType(ComponentType componentType) {
        this.componentType = componentType;
    }

    public void setEsVersion(ESVersion esVersion) {
        this.esVersion = esVersion;
    }
}

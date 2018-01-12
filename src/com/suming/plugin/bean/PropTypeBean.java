package com.suming.plugin.bean;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PropTypeBean {
    // public
    public String name;
    public String type;
    public boolean isRequired;

    // private
    private String describe;
    private String defaultValue;

    // special data
    private List<ShapePropType> shapePropTypeList;

    public PropTypeBean(String name, String type, boolean isRequired) {
        this.name = name;
        this.type = type;
        this.isRequired = isRequired;
    }

    public PropTypeBean(String name, String type, boolean isRequired, String describe) {
        this.name = name;
        this.type = type;
        this.isRequired = isRequired;
        this.describe = describe;
    }

    public PropTypeBean(String name, String type, boolean isRequired, String describe, String defaultValue) {
        this.name = name;
        this.type = type;
        this.isRequired = isRequired;
        this.describe = describe;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    @Nullable
    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public List<ShapePropType> getShapePropTypeList() {
        return shapePropTypeList;
    }

    public void setShapePropTypeList(List<ShapePropType> shapePropTypeList) {
        this.shapePropTypeList = shapePropTypeList;
    }
}

package com.suming.plugin.bean;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PropTypeBean extends BasePropType{
    // private
    private String describe;
    private String defaultValue;

    // special data
    private List<BasePropType> shapePropTypeList;

    public PropTypeBean(String name, String type, boolean isRequired) {
        super(name, type, isRequired);
    }

    public PropTypeBean(String name, String type, boolean isRequired, String describe) {
        super(name, type, isRequired);
        this.describe = describe;
    }

    public PropTypeBean(String name, String type, boolean isRequired, String describe, String defaultValue) {
        super(name, type, isRequired);
        this.describe = describe;
        this.defaultValue = defaultValue;
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

    public List<BasePropType> getShapePropTypeList() {
        return shapePropTypeList;
    }

    public void setShapePropTypeList(List<BasePropType> shapePropTypeList) {
        this.shapePropTypeList = shapePropTypeList;
    }
}

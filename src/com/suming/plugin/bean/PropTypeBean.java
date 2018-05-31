package com.suming.plugin.bean;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PropTypeBean extends BasePropType{
    // private
    private String defaultValue;

    // special data
    private List<BasePropType> shapePropTypeList;

    public PropTypeBean(String name) {
        super(name, "any", false);
    }

    public PropTypeBean(String name, String type, boolean isRequired) {
        super(name, type, isRequired);
    }


    public PropTypeBean(String name, String type, boolean isRequired, String defaultValue) {
        super(name, type, isRequired);
        this.defaultValue = defaultValue;
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

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof PropTypeBean){
            return ((PropTypeBean) obj).name.equals(this.name);
        }
        return super.equals(obj);
    }
}

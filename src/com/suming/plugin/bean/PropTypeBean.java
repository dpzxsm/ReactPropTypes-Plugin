package com.suming.plugin.bean;

public class PropTypeBean {
    public String name;
    public String type;
    public boolean isRequired;

    public PropTypeBean(Object name, Object type, Object isRequired) {
        this.name = name.toString();
        this.type = type.toString();
        this.isRequired = isRequired.toString().equals("true");
    }
}

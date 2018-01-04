package com.suming.plugin.bean;

public class PropTypeBean {
    public String name;
    public String type;
    public boolean isRequired;

    public String describe;

    public PropTypeBean(String name, String type, boolean isRequired) {
        this.name = name;
        this.type = type;
        this.isRequired = isRequired;
    }

    public String getName() {
        return name;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }
}

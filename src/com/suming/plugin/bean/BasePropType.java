package com.suming.plugin.bean;

public class BasePropType {
    public String name;
    public String type;
    public boolean isRequired;

    // BasePropType's JSON String or the Other value
    private String jsonData;

    public BasePropType(String name, String type, boolean isRequired) {
        this.name = name;
        this.type = type;
        this.isRequired = isRequired;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (type != null && !type.equals("")) {
            this.type = type;
        } else {
            this.type = "any";
        }
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        isRequired = required;
    }

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }
}

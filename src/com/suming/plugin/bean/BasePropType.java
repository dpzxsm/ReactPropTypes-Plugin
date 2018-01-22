package com.suming.plugin.bean;

public class BasePropType {
  public String name;
  public String type;
  public boolean isRequired;

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
    this.type = type;
  }

  public boolean isRequired() {
    return isRequired;
  }

  public void setRequired(boolean required) {
    isRequired = required;
  }
}

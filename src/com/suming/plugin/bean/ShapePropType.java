package com.suming.plugin.bean;

public class ShapePropType {
  // public
  public String name;
  public String type;
  public boolean isRequired;

  public ShapePropType(String name, String type, boolean isRequired) {
    this.name = name;
    this.type = type;
    this.isRequired = isRequired;
  }
}

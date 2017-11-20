package com.suming.plugin.bean;

public enum ImportMode {
    Disabled("Disabled"),
    OldModules("React.PropTypes"),
    NewModules("prop-types");

    private final String value;

    ImportMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ImportMode toEnum(String value){
        if(value == null) return null;
        if(value.equals(Disabled.value)){
            return Disabled;
        }else if(value.equals(OldModules.value)){
            return OldModules;
        }else if(value.equals(NewModules.value)){
            return NewModules;
        }else {
            return null;
        }
    }
}

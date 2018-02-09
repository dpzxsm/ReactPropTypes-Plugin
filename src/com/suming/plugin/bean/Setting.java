package com.suming.plugin.bean;

public class Setting {

    private ESVersion esVersion = ESVersion.ES6;
    private ImportMode importMode = ImportMode.Disabled;

    private int indent = 2;

    private boolean noSemiColons = true;
    private boolean isNeedDefault = false;

    public Setting() {
    }

    public ESVersion getEsVersion() {
        return esVersion;
    }

    public void setEsVersion(ESVersion esVersion) {
        this.esVersion = esVersion;
    }

    public ImportMode getImportMode() {
        return importMode;
    }

    public void setImportMode(ImportMode importMode) {
        this.importMode = importMode;
    }

    public int getIndent() {
        return indent;
    }

    public void setIndent(int indent) {
        this.indent = indent;
    }

    public boolean isNeedDefault() {
        return isNeedDefault;
    }

    public void setNeedDefault(boolean needDefault) {
        isNeedDefault = needDefault;
    }

    public boolean isNoSemiColons() {
        return noSemiColons;
    }

    public void setNoSemiColons(boolean noSemiColons) {
        this.noSemiColons = noSemiColons;
    }
}

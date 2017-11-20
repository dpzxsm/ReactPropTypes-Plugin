package com.suming.plugin.bean;

public class Setting {

    ESVersion esVersion;
    ImportMode importMode;

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
}

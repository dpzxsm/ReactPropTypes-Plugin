package com.suming.plugin.ui.config;

import com.suming.plugin.bean.ESVersion;
import com.suming.plugin.bean.ImportMode;
import com.suming.plugin.bean.Setting;

import javax.swing.*;

public class SettingForm {
  private JPanel root;
  private JComboBox importBox;
  private JComboBox esVersionBox;
  private JTextField indentInput;
  private JCheckBox noSemiColonsCheckBox;
  private JCheckBox defaultPropsCheckBox;
  private JCheckBox inferTypeByPropsDestructure;
  private JCheckBox inferTypeByDefaultProps;
  private JCheckBox inferTypeByPropsCall;
  private JCheckBox uncheckedFunctionalComponent;

  JPanel getRoot() {
    return root;
  }

  SettingForm(Setting setting) {
    this.updateSetting(setting);
  }

  void updateSetting(Setting setting){
    importBox.setSelectedItem(setting.getImportMode().getValue());
    esVersionBox.setSelectedItem(setting.getEsVersion().toString());
    indentInput.setText(setting.getIndent() + "");
    noSemiColonsCheckBox.setSelected(setting.isNoSemiColons());
    defaultPropsCheckBox.setSelected(setting.isNeedDefault());
    inferTypeByPropsDestructure.setSelected(setting.isInferByDestructure());
    inferTypeByDefaultProps.setSelected(setting.isInferByDefaultProps());
    inferTypeByPropsCall.setSelected(setting.isInferByPropsCall());
    uncheckedFunctionalComponent.setSelected(setting.isUncheckFunctionalComponent());
  }

  Setting getNewSetting(){
    Setting setting = new Setting();
    ImportMode importMode = importBox.getSelectedItem() == null ?
            ImportMode.Disabled : ImportMode.toEnum(importBox.getSelectedItem().toString());
    ESVersion esVersion = ESVersion.valueOf(esVersionBox.getSelectedItem() !=null ? esVersionBox.getSelectedItem().toString() : "ES6");
    setting.setImportMode(importMode);
    setting.setEsVersion(esVersion);
    setting.setIndent(Integer.parseInt(indentInput.getText()));
    setting.setNoSemiColons(noSemiColonsCheckBox.isSelected());
    setting.setNeedDefault(defaultPropsCheckBox.isSelected());
    setting.setInferByDestructure(inferTypeByPropsDestructure.isSelected());
    setting.setInferByDefaultProps(inferTypeByDefaultProps.isSelected());
    setting.setInferByPropsCall(inferTypeByPropsCall.isSelected());
    setting.setUncheckFunctionalComponent(uncheckedFunctionalComponent.isSelected());
    return setting;
  }
}

package com.suming.plugin.ui.config;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.suming.plugin.bean.Setting;
import com.suming.plugin.persist.SettingService;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SettingEntry implements Configurable{
  private SettingService settingService = ServiceManager.getService(SettingService.class);
  private SettingForm form;

  @Nls
  @Override
  public String getDisplayName() {
    return "ReactPropTypes";
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    SettingService settingService = ServiceManager.getService(SettingService.class);
    form = new SettingForm(settingService.getState());
    return form.getRoot();
  }

  @Override
  public boolean isModified() {
    Setting config =  settingService.getState();
    Setting setting = form !=null ? form.getNewSetting() : null;
    return  setting !=null && (!config.getImportMode().equals(setting.getImportMode())
            || !config.getEsVersion().equals(setting.getEsVersion())
            || config.getIndent() != setting.getIndent()
            || config.isNeedDefault() != setting.isNeedDefault()
            || config.isNoSemiColons() != setting.isNoSemiColons()
            || config.isInferByDestructure() != setting.isInferByDestructure()
            || config.isInferByDefaultProps() != setting.isInferByDefaultProps()
            || config.isInferByPropsCall() != setting.isInferByPropsCall());
  }

  @Override
  public void reset() {
    if(form != null){
      form.updateSetting(settingService.getState());
    }
  }

  @Override
  public void apply() throws ConfigurationException {
    settingService.loadState(form.getNewSetting());
  }

  @Override
  public void disposeUIResources() {
    form = null;
  }
}

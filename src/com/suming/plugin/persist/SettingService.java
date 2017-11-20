package com.suming.plugin.persist;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.suming.plugin.bean.Setting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "SettingVariables",
        storages = {
                @Storage(id = "setting", file = StoragePathMacros.APP_CONFIG +"/Setting.xm")
        }
)

public class SettingService implements PersistentStateComponent<Setting> {
    private Setting mSetting = new Setting();

    public static SettingService getInstance(Project project) {
        return project.getComponent(SettingService.class);
    }

    @NotNull
    @Override
    public Setting getState() {
        return mSetting;
    }

    @Override
    public void loadState(Setting setting) {
       this.mSetting = setting;
    }
}

package com.suming.plugin.persist;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.suming.plugin.bean.Setting;
import org.jetbrains.annotations.NotNull;

@State(name = "SettingVariables",
        storages = {
                @Storage("Setting.xml")
        }
)

public class SettingService implements PersistentStateComponent<Setting> {
    private Setting mSetting = new Setting();

    @NotNull
    @Override
    public Setting getState() {
        return mSetting;
    }

    @Override
    public void loadState(@NotNull Setting setting) {
       this.mSetting = setting;
    }
}

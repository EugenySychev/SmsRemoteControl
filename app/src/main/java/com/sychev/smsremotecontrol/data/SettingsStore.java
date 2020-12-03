package com.sychev.smsremotecontrol.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

public class SettingsStore {

    private static final String APP_NAME = "SmsRemoter";
    private static final String KEY_FILTER_BY_NUMBER_ENABLED = "FilterByNumber";
    private static final String KEY_PASSWORD_ENABLED = "PasswordEnabled";
    private static SettingsStore mInstance;
    private SharedPreferences mPreferences = null;

    public static synchronized SettingsStore getInstance() {
        if (mInstance == null)
            mInstance = new SettingsStore();
        return mInstance;
    }

    public void init(Context context) {
        mPreferences = context.getSharedPreferences(APP_NAME, 0);
    }

    public boolean isInitialized() {
        return mPreferences != null;
    }

    private SettingsStore() {
    }

    public boolean getIsFilterByNumber() {
        return mPreferences.getBoolean(KEY_FILTER_BY_NUMBER_ENABLED, false);
    }

    public void setIsFilterByNumber(boolean val) {
        mPreferences.edit().putBoolean(KEY_FILTER_BY_NUMBER_ENABLED, val).apply();
    }

    public boolean getPasswordEnabled() {
        return mPreferences.getBoolean(KEY_PASSWORD_ENABLED, false);
    }
}

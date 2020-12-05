package com.sychev.smsremotecontrol.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static android.accounts.AccountManager.KEY_PASSWORD;

public class SettingsStore {

    private static final String APP_NAME = "SmsRemoter";
    private static final String KEY_FILTER_BY_NUMBER_ENABLED = "FilterByNumber";
    private static final String KEY_PASSWORD_ENABLED = "PasswordEnabled";
    private static final String KEY_PHONE_COUNT = "PhoneCount";
    private static final String KEY_PHONE_NUMBER = "PhoneNumber";
    private static final String KEY_RESPONSE_ENABLED = "ResponseEnabled";

    private static SettingsStore mInstance;
    private SharedPreferences mPreferences = null;
    private int phoneNumbersCount;
    private static final List<String> phoneNumbers = new ArrayList<>();

    public static synchronized SettingsStore getInstance() {
        if (mInstance == null)
            mInstance = new SettingsStore();
        return mInstance;
    }

    public void init(Context context) {
        mPreferences = context.getSharedPreferences(APP_NAME, 0);

        loadPhones();
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

    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void addPhoneNumber(String number) {
        if (!phoneNumbers.contains(number)) {
            phoneNumbers.add(number);
            resetPhones();
            storePhones();
        }
    }

    public void deletePhoneNumber(String item) {
        if (phoneNumbers.contains(item)) {
            phoneNumbers.remove(item);
            resetPhones();
            storePhones();
        }
    }

    private void loadPhones() {
        phoneNumbersCount = mPreferences.getInt(KEY_PHONE_COUNT, 0);
        phoneNumbers.clear();
        for (int i = 0; i < phoneNumbersCount; i++)
        {
            String loadedNumber = mPreferences.getString(KEY_PHONE_NUMBER+i, "");
            if (loadedNumber != null && !loadedNumber.isEmpty())
                phoneNumbers.add(mPreferences.getString(KEY_PHONE_NUMBER+i, ""));
        }
    }

    private void resetPhones() {
        phoneNumbersCount = mPreferences.getInt(KEY_PHONE_COUNT, 0);
        SharedPreferences.Editor editor = mPreferences.edit();
        for (int i = 0; i < phoneNumbersCount; i++)
            editor.remove(KEY_PHONE_NUMBER + i);
        editor.putInt(KEY_PHONE_COUNT, 0);
        editor.apply();
    }

    private void storePhones() {
        phoneNumbersCount = phoneNumbers.size();
        SharedPreferences.Editor editor = mPreferences.edit();
        for (int i = 0; i < phoneNumbersCount; i++)
            editor.putString(KEY_PHONE_NUMBER + i, phoneNumbers.get(i));
        editor.putInt(KEY_PHONE_COUNT, phoneNumbersCount);
        editor.apply();
    }

    public void setPasswordEnabled(boolean checked) {
        mPreferences.edit().putBoolean(KEY_PASSWORD_ENABLED, checked).apply();
    }

    public void setPassword(String text) {
        mPreferences.edit().putString(KEY_PASSWORD, text).apply();
    }

    public String getPassword() {
        return mPreferences.getString(KEY_PASSWORD, "");
    }

    public void setResponseEnabled(boolean isChecked) {
        mPreferences.edit().putBoolean(KEY_RESPONSE_ENABLED, isChecked).apply();
    }

    public boolean getResponseEnabled() {
        return mPreferences.getBoolean(KEY_RESPONSE_ENABLED, false);
    }
}

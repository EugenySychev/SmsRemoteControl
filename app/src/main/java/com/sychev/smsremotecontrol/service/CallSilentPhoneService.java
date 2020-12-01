package com.sychev.smsremotecontrol.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class CallSilentPhoneService extends Service  implements SmsReceiver.SmsHandler {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        loadSettings();

        return super.onStartCommand(intent, flags, startId);
    }

    private void loadSettings() {
    }

    @Override
    public void processIncomingSms(String number, String sms) {

    }
}

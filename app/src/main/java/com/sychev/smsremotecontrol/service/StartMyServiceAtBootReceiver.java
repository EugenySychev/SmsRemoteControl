package com.sychev.smsremotecontrol.service;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.Messenger;
import android.widget.Toast;

import com.sychev.smsremotecontrol.data.SettingsStore;

import java.util.Set;

public class StartMyServiceAtBootReceiver extends BroadcastReceiver {
    Messenger messenger = null;
    boolean bound;
    private final ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            messenger = new Messenger(service);
            bound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            messenger = null;
            bound = false;
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!SettingsStore.getInstance().isInitialized())
            SettingsStore.getInstance().init(context);
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) &&
                SettingsStore.getInstance().getServiceEnabling()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(new Intent(context, SmsHandlingService.class));
            } else {
                context.startService(new Intent(context, SmsHandlingService.class));
            }
        }
    }
}
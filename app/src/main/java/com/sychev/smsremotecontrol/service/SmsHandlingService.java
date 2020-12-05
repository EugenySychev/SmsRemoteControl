package com.sychev.smsremotecontrol.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.provider.Telephony;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.sychev.smsremotecontrol.MainActivity;
import com.sychev.smsremotecontrol.R;
import com.sychev.smsremotecontrol.data.SettingsStore;

public class SmsHandlingService extends Service implements SmsReceiver.SmsHandler {

    public static final int RELOAD_SETTINGS = 1;
    private static final String TAG = "SmsService";
    private static final String CHANNEL_ID = "ForegroundServiceChannel";
    private boolean started = false;

    private final IBinder mBinder = new SmsServiceBinder();

    public boolean getIsRunning() {
        return started;
    }

    public class SmsServiceBinder extends Binder {
        public SmsHandlingService getService() {
            return SmsHandlingService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void reloadSettings() {
        Log.d(TAG, "Reload settings");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (!SettingsStore.getInstance().isInitialized())
            SettingsStore.getInstance().init(this);

        loadSettings();

        SmsReceiver receiver = new SmsReceiver();
        receiver.setSmsHandler(this);
        registerReceiver(receiver, new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));
        Toast.makeText(this, "Service started", Toast.LENGTH_LONG).show();
        Log.d(TAG, "Service started");


        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getResources().getString(R.string.notification_title))
                .setContentText(getResources().getString(R.string.notification_service_started))
                .setSmallIcon(R.drawable.ic_baseline_settings_remote_24)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
        started = true;
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        started = false;
        Toast.makeText(this, "Service stoped", Toast.LENGTH_LONG).show();
    }

    private void loadSettings() {
    }

    @Override
    public void processIncomingSms(String number, String sms) {
        Toast.makeText(this, "Received sms ", Toast.LENGTH_LONG).show();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Received message from " + number)
                .setContentText(sms)
                .setSmallIcon(R.drawable.ic_baseline_visibility_24)
                .setContentIntent(pendingIntent)
                .build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

// notificationId is a unique int for each notification that you must define
        notificationManager.notify(0, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}

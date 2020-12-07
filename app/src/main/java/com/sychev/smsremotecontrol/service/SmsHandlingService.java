package com.sychev.smsremotecontrol.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.provider.Telephony;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.sychev.smsremotecontrol.MainActivity;
import com.sychev.smsremotecontrol.R;
import com.sychev.smsremotecontrol.data.SettingsStore;

import java.util.HashMap;

public class SmsHandlingService extends Service implements SmsReceiver.SmsHandler {

    public static final int RELOAD_SETTINGS = 1;
    private static final String TAG = "SmsService";
    private static final String CHANNEL_ID = "ForegroundServiceChannel";
    private static final int NUMBER_WORD_SMS_COMMAND = 3;
    private static final String SET_VOLUME_COMMAND = "setvolume";
    private VolumeAdapter volumeAdapter;
    private boolean started = false;
    private SmsReceiver smsReceiver;
    private final IBinder mBinder = new SmsServiceBinder();

    private enum Result {
        OK("OK", 0),
        INCORRECT_COMMAND("Wrong command", 1),
        INCORRECT_PASSWORD("Incorrect password", 2),
        INCORRECT_ARGUMENTS("Incorrect argument", 3),
        GENERAL_FAILURE("General failure", 4),
        INCORRECT_USER("Wrong user", 5);

        private static final HashMap<String, Result> by_message = new HashMap<String, Result>();
        private static final HashMap<Integer, Result> by_value = new HashMap<Integer, Result>();

        static {
            for (Result res : values()) {
                by_message.put(res.mess, res);
                by_value.put(res.value, res);
            }
        }

        public final String mess;
        public final int value;

        Result(String message, int val) {
            mess = message;
            value = val;
        }

        public static Result valueOfVal(int value) {
            return by_value.get(value);
        }

        public static Result valueOfString(String str) {
            return by_message.get(str);
        }

        @Override
        public String toString() {
            return this.mess;
        }
    }

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

        smsReceiver = new SmsReceiver();
        smsReceiver.setSmsHandler(this);
        registerReceiver(smsReceiver, new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));
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

        volumeAdapter = new VolumeAdapter(this);
        started = true;

        return START_STICKY;
    }

    public void onStop() {
        started = false;
        unregisterReceiver(smsReceiver);
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
        Result res = Result.INCORRECT_USER;
        res = checkPasswordNeeded(sms);
        if (res == Result.OK) {
            res = checkFilterNumber(number);
            if (res == Result.OK) {
                String[] strings = sms.split(" ");
                int startInd = SettingsStore.getInstance().getPasswordEnabled() ? 1 : 0;
                if (strings.length == NUMBER_WORD_SMS_COMMAND + startInd) {
                    String command = strings[startInd]; // dropped password if needed
                    String type = strings[startInd + 1];
                    int value = Integer.parseInt(strings[startInd + 2]);
                    if (SettingsStore.getInstance().getVolumeControlEnabled() &&
                            command.equalsIgnoreCase(SET_VOLUME_COMMAND) &&
                            volumeAdapter != null) {

                        if (type.equalsIgnoreCase("ring") ||
                                type.equalsIgnoreCase("media") ||
                                type.equalsIgnoreCase("alarm")) {
                            try {
                                volumeAdapter.setVolume(value, type);
                                res = Result.OK;
                            } catch (Exception e) {
                                res = Result.GENERAL_FAILURE;
                            }
                        } else {
                            res = Result.INCORRECT_ARGUMENTS;
                        }
                    } else {
                        res = Result.INCORRECT_COMMAND;
                    }
                } else {
                    res = Result.INCORRECT_COMMAND;
                }
            }
        }

        if (SettingsStore.getInstance().getResponseEnabled()) {
            sendResponse(res, number);
        }
    }

    private void sendResponse(Result res, String number) {
        if (res != Result.INCORRECT_USER) {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                String msg = res.toString();
                smsManager.sendTextMessage(number, null, msg, null, null);
                Toast.makeText(getApplicationContext(), "Message Sent",
                        Toast.LENGTH_LONG).show();
            } catch (Exception ex) {
                Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
                        Toast.LENGTH_LONG).show();
                ex.printStackTrace();
            }
        }
    }

    private Result checkFilterNumber(String number) {
        if (SettingsStore.getInstance().getIsFilterByNumber()) {
            for (String numberInList : SettingsStore.getInstance().getPhoneNumbers()) {
                if (PhoneNumberUtils.compare(number, numberInList)) {
                    return Result.OK;
                }
            }
            return Result.INCORRECT_USER;
        }
        return Result.OK;
    }

    private Result checkPasswordNeeded(String sms) {
        if (SettingsStore.getInstance().getPasswordEnabled()) {
            String pass = SettingsStore.getInstance().getPassword();
            if (sms.substring(0, pass.length()).equals(pass))
                return Result.OK;
            return Result.INCORRECT_PASSWORD;
        }
        return Result.OK;
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

package com.sychev.smsremotecontrol;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.sychev.smsremotecontrol.data.SettingsStore;
import com.sychev.smsremotecontrol.service.SmsHandlingService;
import com.sychev.smsremotecontrol.view.ContactSelectionActivity;
import com.yandex.mobile.ads.AdRequest;
import com.yandex.mobile.ads.AdSize;
import com.yandex.mobile.ads.AdView;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "Main";
    private static final int MY_PERMISSIONS_REQUEST = 1;
    private SmsHandlingService mService;
    private boolean mBound;
    private boolean mServiceShouldRun;
    private static final int NOTIF_POLICY_PERMISSIONS_REQUEST = 2;
    private SwitchCompat volumeControlSwitch;
    private SwitchCompat sendResponseSwitch;
    private SwitchCompat switchFilterNumbers;
    private static final String blockId = "R-M-692728-1";
    private AudioManager audioManager;

    private final ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            SmsHandlingService.SmsServiceBinder binder = (SmsHandlingService.SmsServiceBinder) service;
            mService = binder.getService();
            if (!mService.getIsRunning() && mServiceShouldRun)
                startService();
            mBound = true;
//            Toast.makeText(MainActivity.this, "service connected", Toast.LENGTH_LONG).show();

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
//            Toast.makeText(MainActivity.this, "service disconnected", Toast.LENGTH_LONG).show();
        }
    };
    ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SettingsStore.getInstance().init(this);
        mServiceShouldRun = SettingsStore.getInstance().getServiceEnabling();


        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        connectToService();
        setupContactFilteSwitch();
        setupPasswordInput();
        setupCheckServiceStateButton();
        setupResponseSwitch();
        setupVolumeControlSwitch();
        setupDescriptionVolumeSwitch();
        setupYandexAds();
    }

    private void connectToService() {
        Intent intent = new Intent(this, SmsHandlingService.class);
        int flags = Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT;
        bindService(intent, connection, flags);
    }

    private void setupContactFilteSwitch() {
        AppCompatButton buttonFilterContactActivity = findViewById(R.id.showSelectContactButton);
        buttonFilterContactActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ContactSelectionActivity.class);
                startActivity(intent);
            }
        });
        switchFilterNumbers = findViewById(R.id.filterNumberSwitch);
        switchFilterNumbers.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonFilterContactActivity.setVisibility(switchFilterNumbers.isChecked() ? View.VISIBLE : View.GONE);
                SettingsStore.getInstance().setIsFilterByNumber(switchFilterNumbers.isChecked());

                if (isChecked)
                    checkPermissionManifest(Manifest.permission.READ_CONTACTS);
            }
        });
        switchFilterNumbers.setChecked(SettingsStore.getInstance().getIsFilterByNumber());
        buttonFilterContactActivity.setVisibility(switchFilterNumbers.isChecked() ? View.VISIBLE : View.GONE);
    }

    private void setupPasswordInput() {
        TextInputEditText passwordEdit = findViewById(R.id.passwordEdit);
        AppCompatButton savePasswordButton = findViewById(R.id.savePasswordButton);
        savePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsStore.getInstance().setPassword(passwordEdit.getText().toString());
            }
        });
        passwordEdit.setText(SettingsStore.getInstance().getPassword());

        TextInputLayout passLayoutt = findViewById(R.id.etPasswordLayout);
        SwitchCompat switchPasswordRequired = findViewById(R.id.needPasswordSwitch);
        switchPasswordRequired.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                passLayoutt.setVisibility(switchPasswordRequired.isChecked() ? View.VISIBLE : View.GONE);
                SettingsStore.getInstance().setPasswordEnabled(switchPasswordRequired.isChecked());
            }
        });

        switchPasswordRequired.setChecked(SettingsStore.getInstance().getPasswordEnabled());
        passLayoutt.setVisibility(switchPasswordRequired.isChecked() ? View.VISIBLE : View.GONE);
    }

    private void setupCheckServiceStateButton() {
//        TextView serviceStateTextView = findViewById(R.id.serviceStateTextView);
//        AppCompatButton button = findViewById(R.id.checkServiceStateButton);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mBound) {
//                    serviceStateTextView.setText("Service binded" + (mService.getIsRunning() ? " started" : " stoped"));
//                } else {
//                    serviceStateTextView.setText("Something goes wrong" + (mService.getIsRunning() ? " started" : " stoped"));
//                }
//            }
//        });
    }

    private void setupVolumeControlSwitch() {
        volumeControlSwitch = findViewById(R.id.volumeControlSwitch);
        volumeControlSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingsStore.getInstance().setVolumeControlEnabled(isChecked);

                if (isChecked) {
                    checkPermissionManifest(Manifest.permission.ACCESS_NOTIFICATION_POLICY);

                    checkPermissionManifest(Manifest.permission.RECEIVE_SMS);
                    checkPermissionManifest(Manifest.permission.READ_SMS);

                    NotificationManager n = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    if (!n.isNotificationPolicyAccessGranted()) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setMessage(R.string.alert_notification_policy)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent acNPIntent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                                        MainActivity.this.startActivityForResult(acNPIntent, NOTIF_POLICY_PERMISSIONS_REQUEST);
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                })
                                .setCancelable(true)
                                .create()
                                .show();
                    }
                }
                if (mService != null)
                    mService.reloadSettings();
                if (mService != null) {
                    if (SettingsStore.getInstance().getServiceEnabling() && !mService.getIsRunning())
                        startService();
                    if (!SettingsStore.getInstance().getServiceEnabling() && mService.getIsRunning())
                        stopService();
                }
            }
        });
        volumeControlSwitch.setChecked(SettingsStore.getInstance().getVolumeControlEnabled());
    }

    private void setupYandexAds() {
        //        YandexMetricaConfig config = YandexMetricaConfig.newConfigBuilder(API_key).build();
//        // Initializing the AppMetrica SDK.
//        YandexMetrica.activate(getApplicationContext(), config);
//        // Automatic tracking of user activity.
//        YandexMetrica.enableActivityAutoTracking(this);
        AdView mAdView = (AdView) findViewById(R.id.ad_view);
        mAdView.setBlockId(blockId);
        mAdView.setAdSize(AdSize.stickySize(AdSize.FULL_WIDTH));

        // Создание объекта таргетирования рекламы.
        final AdRequest adRequest = new AdRequest.Builder().build();

        // Регистрация слушателя для отслеживания событий, происходящих в баннерной рекламе.
//        mAdView.setAdEventListener(new AdEventListener.SimpleAdEventListener() {
//            @Override
//            public void onAdLoaded() {
//            }
//        });

        // Загрузка объявления.
        mAdView.loadAd(adRequest);
    }

    private void setupDescriptionVolumeSwitch() {
        TextView volumeDescrView = findViewById(R.id.descriptionVolumeSwitcher);
        String volumeDescriptionString = getString(R.string.this_switch);
        volumeDescriptionString +=
                audioManager.getStreamMaxVolume(AudioManager.STREAM_RING) + getString(R.string.for_ring) +
                        audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) + getString(R.string.for_music) +
                        audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM) + getString(R.string.for_alarm);
        volumeDescrView.setText(volumeDescriptionString);
    }

    private void setupResponseSwitch() {
        sendResponseSwitch = findViewById(R.id.responseSwitch);
        sendResponseSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingsStore.getInstance().setResponseEnabled(isChecked);

                if (isChecked)
                    checkPermissionManifest(Manifest.permission.SEND_SMS);
            }
        });
        sendResponseSwitch.setChecked(SettingsStore.getInstance().getResponseEnabled());
    }

    private void startService() {
        startForegroundService(new Intent(MainActivity.this, SmsHandlingService.class));
    }

    private void stopService() {
        mService.onStop();
        mService.stopForeground(Service.STOP_FOREGROUND_REMOVE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NOTIF_POLICY_PERMISSIONS_REQUEST) {
            if (!((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).isNotificationPolicyAccessGranted() && volumeControlSwitch != null)
                volumeControlSwitch.setChecked(false);
        }
    }

    private void checkPermissionManifest(String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{permission},
                    MY_PERMISSIONS_REQUEST);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {

                    if (sendResponseSwitch.isChecked() && permissions[i].equals(Manifest.permission.SEND_SMS))
                        sendResponseSwitch.setChecked(false);
                    if (sendResponseSwitch.isChecked() && permissions[i].equals(Manifest.permission.READ_CONTACTS))
                        switchFilterNumbers.setChecked(false);

                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(connection);
            mBound = false;
        }

    }
}
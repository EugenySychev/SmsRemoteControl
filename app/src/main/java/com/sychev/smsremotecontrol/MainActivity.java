package com.sychev.smsremotecontrol;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SwitchCompat;

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.sychev.smsremotecontrol.data.SettingsStore;
import com.sychev.smsremotecontrol.service.SmsHandlingService;
import com.sychev.smsremotecontrol.view.ContactSelectionActivity;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "Main";
    private static final int MY_PERMISSIONS_REQUEST = 1;
    private SmsHandlingService mService;
    private boolean mBound;

    private SwitchCompat sendResponseSwitch;

    private final ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SmsHandlingService.SmsServiceBinder binder = (SmsHandlingService.SmsServiceBinder) service;
            mService = binder.getService();
            if (!mService.getIsRunning() && mServiceShouldRun)
                startService();
            mBound = true;
            Toast.makeText(MainActivity.this, "service connected", Toast.LENGTH_LONG).show();

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            Toast.makeText(MainActivity.this, "service disconnected", Toast.LENGTH_LONG).show();
        }
    };
    private boolean mServiceShouldRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SettingsStore.getInstance().init(this);
        mServiceShouldRun = SettingsStore.getInstance().getServiceEnabling();

        Intent intent = new Intent(this, SmsHandlingService.class);
        int flags = Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT;
        bindService(intent, connection, flags);

        AppCompatButton buttonFilterContactActivity = findViewById(R.id.showSelectContactButton);
        buttonFilterContactActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ContactSelectionActivity.class);
                startActivity(intent);
            }
        });
        SwitchCompat switchFilterNumbers = findViewById(R.id.filterNumberSwitch);
        switchFilterNumbers.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonFilterContactActivity.setVisibility(switchFilterNumbers.isChecked() ? View.VISIBLE : View.GONE);
                SettingsStore.getInstance().setIsFilterByNumber(switchFilterNumbers.isChecked());
            }
        });
        switchFilterNumbers.setChecked(SettingsStore.getInstance().getIsFilterByNumber());
        buttonFilterContactActivity.setVisibility(switchFilterNumbers.isChecked() ? View.VISIBLE : View.GONE);


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

        TextView serviceStateTextView = findViewById(R.id.serviceStateTextView);
        AppCompatButton button = findViewById(R.id.checkServiceStateButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBound) {
                    serviceStateTextView.setText("Service binded" + (mService.getIsRunning() ? " started" : " stoped"));
                } else {
                    serviceStateTextView.setText("Something goes wrong" + (mService.getIsRunning() ? " started" : " stoped"));
                }
            }
        });

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

        SwitchCompat volumeControlSwitch = findViewById(R.id.volumeControlSwitch);
        volumeControlSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingsStore.getInstance().setVolumeControlEnabled(isChecked);
                checkPermissionManifest(Manifest.permission.ACCESS_NOTIFICATION_POLICY);
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

    private void startService() {
        startForegroundService(new Intent(MainActivity.this, SmsHandlingService.class));
    }

    private void stopService() {
        mService.onStop();
        mService.stopForeground(Service.STOP_FOREGROUND_REMOVE);
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

                }
            }

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
        mBound = false;
    }
}
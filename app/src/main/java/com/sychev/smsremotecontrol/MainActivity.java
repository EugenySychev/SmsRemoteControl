package com.sychev.smsremotecontrol;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.sychev.smsremotecontrol.data.SettingsStore;
import com.sychev.smsremotecontrol.service.SmsHandlingService;
import com.sychev.smsremotecontrol.view.ContactSelectionActivity;

public class MainActivity extends AppCompatActivity {


    private SmsHandlingService mService;
    private boolean mBound;
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SmsHandlingService.SmsServiceBinder binder = (SmsHandlingService.SmsServiceBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SettingsStore.getInstance().init(this);

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


        AppCompatEditText passwordEdit = findViewById(R.id.passwordEdit);

        SwitchCompat switchPasswordRequired = findViewById(R.id.needPasswordSwitch);
        switchPasswordRequired.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                passwordEdit.setVisibility(switchPasswordRequired.isChecked() ? View.VISIBLE : View.GONE);
                SettingsStore.getInstance().setPasswordEnabled(switchPasswordRequired.isChecked());
            }
        });

        switchPasswordRequired.setChecked(SettingsStore.getInstance().getPasswordEnabled());
        passwordEdit.setVisibility(switchPasswordRequired.isChecked() ? View.VISIBLE : View.GONE);

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

                if (!mService.getIsRunning())
                    startForegroundService(intent);

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
//        unbindService(connection);
        mBound = false;
    }
}
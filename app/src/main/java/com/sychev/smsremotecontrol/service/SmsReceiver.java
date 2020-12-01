package com.sychev.smsremotecontrol.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {

    private SmsHandler handler = null;

    void setSmsHandler(SmsHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            for (SmsMessage message : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                String from = message.getOriginatingAddress();
                String text = message.getMessageBody();
                if (handler != null)
                    handler.processIncomingSms(from, text);
            }
        }
    }

    interface SmsHandler {
        void processIncomingSms(String number, String sms);
    }
}

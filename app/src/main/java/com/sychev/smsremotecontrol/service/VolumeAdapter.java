package com.sychev.smsremotecontrol.service;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.provider.Settings;
import android.util.Log;


public class VolumeAdapter {

    private static final String TAG = "AUDIO";
    private final AudioManager audioManager;
    private final MediaPlayer playerRing;

    public VolumeAdapter(Context context) {
        playerRing = MediaPlayer.create(context, Settings.System.DEFAULT_RINGTONE_URI);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public void setVolume(int volume, String type) {
        int mediaType = AudioManager.STREAM_SYSTEM;
        if (type.equalsIgnoreCase("ring"))
            mediaType = AudioManager.STREAM_RING;
        else if (type.equalsIgnoreCase("media"))
            mediaType = AudioManager.STREAM_MUSIC;
        else if (type.equalsIgnoreCase("alarm"))
            mediaType = AudioManager.STREAM_ALARM;

        audioManager.setStreamVolume(mediaType, volume, audioManager.getStreamMaxVolume(mediaType));
    }

    public void playRingtone() {
        playerRing.start();
    }


}

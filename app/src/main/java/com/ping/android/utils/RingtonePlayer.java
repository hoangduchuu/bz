package com.ping.android.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;

import com.quickblox.videochat.webrtc.AppRTCAudioManager;

import java.io.IOException;
import java.util.stream.Stream;


/**
 * QuickBlox team
 */
public class RingtonePlayer {

    private static final String TAG = RingtonePlayer.class.getSimpleName();
    private MediaPlayer mediaPlayer;
    private Handler handler;
    private boolean looping = false;
    private Context context;
    private int ringingMode = -1;
    AudioManager audioManager;

    private final Runnable loopingRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                //audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                mediaPlayer.start();
            }
        }
    };

    public RingtonePlayer(Context context, int resource) {
        this.context = context;
        mediaPlayer = android.media.MediaPlayer.create(context, resource);
        handler = new Handler();
        audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        AudioAttributes atrs = (new AudioAttributes.Builder()).setLegacyStreamType(AudioManager.STREAM_VOICE_CALL).build();
        mediaPlayer.setAudioAttributes(atrs);
        int maxVolumeMusic = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        ringingMode = audioManager.getRingerMode();
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolumeMusic,AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (!looping){
                    return;
                }
                handler.postDelayed(loopingRunnable, 5000);
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {

                return true;
            }
        });
        play(true);
    }

    public RingtonePlayer(Context context) {
        this.context = context;
        Uri notification = getNotification();
        if (notification != null) {
            mediaPlayer = android.media.MediaPlayer.create(context, notification);
        }
    }

    private Uri getNotification() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

        if (notification == null) {
            // notification is null, using backup
            notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            // I can't see this ever being null (as always have a default notification)
            // but just incase
            if (notification == null) {
                // notification backup is null, using 2nd backup
                notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            }
        }
        return notification;
    }

    public void play(boolean looping) {
        Log.i(TAG, "play");
        if (mediaPlayer == null) {
            Log.i(TAG, "mediaPlayer isn't created ");
            return;
        }
        this.looping = looping;
        mediaPlayer.start();
    }

    public synchronized void stop() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            mediaPlayer.release();
            if (audioManager != null){
                if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL && ringingMode >= 0){
                    audioManager.setRingerMode(ringingMode);
                }
            }
            mediaPlayer = null;
        }
    }
}

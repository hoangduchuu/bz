package com.ping.android.gcm;

import android.os.Bundle;
import android.util.Log;

import com.ping.android.utils.ActivityLifecycle;
import com.quickblox.messages.services.gcm.QBGcmPushListenerService;

public abstract class PGcmPushListenerService extends QBGcmPushListenerService {
    private static final String TAG = PGcmPushListenerService.class.getSimpleName();

    @Override
    public void sendPushMessage(Bundle data, String from, String message) {
        super.sendPushMessage(data, from, message);
        Log.v(TAG, "From: " + from);
        Log.v(TAG, "Message: " + message);

        if (ActivityLifecycle.getInstance().isBackground()) {
            showNotification(message);
        }
    }

    private static final int NOTIFICATION_ID = 1;


    public void showNotification(String message){
        Log.e("ee", message);
    }


}
package com.ping.android.device.impl;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.ping.android.activity.R;
import com.ping.android.device.Notification;
import com.ping.android.model.Call;
import com.ping.android.presentation.view.activity.CallActivity;

/**
 * Created by tuanluong on 3/27/18.
 */

public class NotificationImpl implements Notification {
    private static final int ONGOING_NOTIFICATION_ID = 1111;
    private Context context;
    private NotificationManager notificationManager;

    public NotificationImpl(Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public void showOngoingCallNotification(String tag) {
        notificationManager.cancel(ONGOING_NOTIFICATION_ID);

        Intent notificationIntent = new Intent(context, CallActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 123,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "call")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Ongoing call...")
                .setColor(context.getResources().getColor(R.color.colorAccent))
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setAutoCancel(false)
                .setContentIntent(pendingIntent);
        Intent endCallIntent = new Intent(context, CallActivity.class);
        endCallIntent.putExtra("ENDCALL", true);
        NotificationCompat.Action endCallAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_action_send_now, "END CALL", PendingIntent.getActivity(context, 100, endCallIntent,
                PendingIntent.FLAG_UPDATE_CURRENT))
                .build();
        builder.addAction(endCallAction);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        android.app.Notification notification = builder.build();
        notification.flags |= android.app.Notification.FLAG_NO_CLEAR;
        notificationManager.notify(tag, ONGOING_NOTIFICATION_ID, notification);
    }

    @Override
    public void cancelOngoingCall(String tag) {
        notificationManager.cancel(tag, ONGOING_NOTIFICATION_ID);
    }
}

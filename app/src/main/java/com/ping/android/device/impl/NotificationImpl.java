package com.ping.android.device.impl;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.ping.android.activity.R;
import com.ping.android.device.Notification;
import com.ping.android.model.Call;
import com.ping.android.presentation.view.activity.CallActivity;
import com.ping.android.presentation.view.activity.ChatActivity;
import com.ping.android.presentation.view.activity.SplashActivity;
import com.ping.android.service.NotificationBroadcastReceiver;
import com.ping.android.utils.ActivityLifecycle;

/**
 * Created by tuanluong on 3/27/18.
 */

public class NotificationImpl implements Notification {
    public static final int ONGOING_NOTIFICATION_ID = 1111;
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
                .setContentIntent(pendingIntent)
                .setCategory(android.app.Notification.CATEGORY_CALL);
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

    @Override
    public void showMissedCallNotification(String opponentUserId, String message,
                                           boolean isVideo, String tag, boolean enableSound) {
        Intent intent = new Intent(context, SplashActivity.class);
        intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "missed_call")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentText(message)
                .setColor(context.getResources().getColor(R.color.colorAccent))
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .setCategory(android.app.Notification.CATEGORY_CALL);
        if (ActivityLifecycle.getInstance().isForeground() && !enableSound) {
            builder.setDefaults(android.app.Notification.DEFAULT_LIGHTS | android.app.Notification.DEFAULT_VIBRATE);
        } else {
            builder.setDefaults(android.app.Notification.DEFAULT_ALL);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.
                    setSmallIcon(R.drawable.ic_notification).
                    setColor(context.getResources().getColor(R.color.colorAccent)).
                    setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
        } else {
            builder.setSmallIcon(R.mipmap.ic_launcher);
        }
        Intent callbackIntent = NotificationBroadcastReceiver.getCallbackIntent(context, opponentUserId, isVideo);
        PendingIntent callbackPendingIntent = PendingIntent.getBroadcast(context, 124,
                callbackIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action callbackAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_action_send_now, "CALLBACK", callbackPendingIntent)
                .build();
        builder.addAction(callbackAction);
        android.app.Notification notification = builder.build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    notificationManager.getNotificationChannel("missed_call");
            if (channel == null) {
                channel = new NotificationChannel("missed_call", "Missed calls", NotificationManager.IMPORTANCE_DEFAULT);
                channel.enableLights(true);
                channel.setLightColor(Color.GREEN);
                channel.enableVibration(true);
                channel.setShowBadge(true);
                notificationManager.createNotificationChannel(channel);
            }
            //builder.setChannelId("missed_call");
        }
        notificationManager.notify(tag, ONGOING_NOTIFICATION_ID, notification);
    }
}

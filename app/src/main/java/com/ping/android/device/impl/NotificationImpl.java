package com.ping.android.device.impl;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ping.android.activity.R;
import com.ping.android.device.Notification;
import com.ping.android.model.Call;
import com.ping.android.model.User;
import com.ping.android.presentation.view.activity.CallActivity;
import com.ping.android.presentation.view.activity.ChatActivity;
import com.ping.android.presentation.view.activity.SplashActivity;
import com.ping.android.service.NotificationBroadcastReceiver;
import com.ping.android.ultility.Callback;
import com.ping.android.utils.ActivityLifecycle;
import com.ping.android.utils.GlideApp;

import java.util.concurrent.atomic.AtomicInteger;

import static com.ping.android.service.NotificationBroadcastReceiver.KEY_REPLY;
import static com.ping.android.utils.ResourceUtils.getString;

/**
 * Created by tuanluong on 3/27/18.
 */

public class NotificationImpl implements Notification {
    public static final int ONGOING_NOTIFICATION_ID = 1111;
    public static final int MESSAGE_NOTIFICATION_ID = 2222;
    private Context context;
    private NotificationManager notificationManager;

    public NotificationImpl(Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private final static AtomicInteger c = new AtomicInteger(0);

    public static int getID() {
        return c.incrementAndGet();
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
        // FIXME: Move to sprint 4
        /*Intent callbackIntent = NotificationBroadcastReceiver.getCallbackIntent(context, opponentUserId, isVideo);
        PendingIntent callbackPendingIntent = PendingIntent.getBroadcast(context, 124,
                callbackIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action callbackAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_action_send_now, "CALL BACK", callbackPendingIntent)
                .build();
        builder.addAction(callbackAction);*/
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

    @Override
    public void showMessageNotification(User user, String message, String conversationId, String senderProfile) {
        boolean soundNotification = user.settings.notification;
        int notificationId = getID();
        // 3. Build notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "message");
        // Create pending intent, mention the Activity which needs to be
        Intent intent = new Intent(context, SplashActivity.class);
        intent.putExtra(ChatActivity.CONVERSATION_ID, conversationId);
        intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
        PendingIntent contentIntent = PendingIntent.getActivity(context, notificationId,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder
                .setContentText(message)
                .setContentIntent(contentIntent)
                // FIXME I want to group all message in conversation to a group but it seems not working
                .setGroup(conversationId)
                //setShowWhen(true).
                //setWhen(0).
                .setAutoCancel(true);
        if (ActivityLifecycle.getInstance().isForeground() && !soundNotification) {
            notificationBuilder.setDefaults(android.app.Notification.DEFAULT_LIGHTS | android.app.Notification.DEFAULT_VIBRATE);
        } else {
            notificationBuilder.setDefaults(android.app.Notification.DEFAULT_ALL);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(NotificationManager.IMPORTANCE_HIGH);
        } else {
            notificationBuilder
                    .setPriority(android.app.Notification.PRIORITY_HIGH);
        }
        //do not show double BZZZ, will change if use title for other meaning
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            notificationBuilder
                    .setContentTitle("BZZZ");
        } else {
            // 1. Build label
            String replyLabel = getString(R.string.notif_action_reply);
            RemoteInput remoteInput = new RemoteInput.Builder(KEY_REPLY)
                    .setLabel(replyLabel)
                    .build();


            Intent intent1 = NotificationBroadcastReceiver.getReplyMessageIntent(context, notificationId, conversationId);
            PendingIntent replyPendingIntent = PendingIntent.getBroadcast(context, notificationId, intent1,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            // 2. Build action
            NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(
                    R.drawable.ic_action_send_now, replyLabel, replyPendingIntent)
                    .addRemoteInput(remoteInput)
                    .setAllowGeneratedReplies(true)
                    .build();
            notificationBuilder.addAction(replyAction);

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    notificationManager.getNotificationChannel("message");
            if (channel == null) {
                channel = new NotificationChannel("message", "Messages", NotificationManager.IMPORTANCE_DEFAULT);
                channel.enableLights(true);
                channel.setLightColor(Color.GREEN);
                channel.enableVibration(true);
                channel.setShowBadge(true);
                notificationManager.createNotificationChannel(channel);
            }
            //builder.setChannelId("missed_call");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> prepareProfileImage(context, senderProfile, (error, data) -> {
                if (error != null) {
                    notificationBuilder.
                            setSmallIcon(R.drawable.ic_notification).
                            setColor(context.getResources().getColor(R.color.colorAccent)).
                            setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
                } else {
                    notificationBuilder.
                            setSmallIcon(R.drawable.ic_notification).
                            setColor(context.getResources().getColor(R.color.colorAccent)).
                            setLargeIcon((Bitmap) data[0]);
                }
                notificationManager.notify(notificationId, notificationBuilder.build());
            }));
        } else {
            notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
            notificationManager.notify(notificationId, notificationBuilder.build());
        }
    }

    private void prepareProfileImage(Context context, String profileImage, Callback callback) {
        if (profileImage != null && !profileImage.isEmpty() && profileImage.startsWith("gs://")) {
            StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(profileImage);
            SimpleTarget<Bitmap> target = new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                    callback.complete(null, resource);
                }

                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    callback.complete(new Error());
                }
            };
            GlideApp.with(context)
                    .asBitmap()
                    .override(100)
                    .apply(RequestOptions.circleCropTransform())
                    .load(gsReference)
                    .into(target);
        } else {
            callback.complete(new Error());
        }
    }
}

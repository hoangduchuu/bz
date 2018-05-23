package com.ping.android.service;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.text.TextUtils;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ping.android.App;
import com.ping.android.R;
import com.ping.android.domain.usecase.GetCurrentUserUseCase;
import com.ping.android.domain.usecase.notification.ShowIncomingMessageNotificationUseCase;
import com.ping.android.domain.usecase.notification.ShowMissedCallNotificationUseCase;
import com.ping.android.model.User;
import com.ping.android.presentation.view.activity.ChatActivity;
import com.ping.android.presentation.view.activity.SplashActivity;
import com.ping.android.model.Callback;
import com.ping.android.utils.ActivityLifecycle;
import com.ping.android.utils.BadgeHelper;
import com.bzzzchat.configuration.GlideApp;
import com.ping.android.utils.Log;
import com.ping.android.utils.SharedPrefsHelper;

import org.json.JSONException;

import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import static com.ping.android.service.NotificationBroadcastReceiver.KEY_REPLY;
import static com.ping.android.utils.ResourceUtils.getString;

/**
 * Created by Tung Tran on 12/2/2017.
 */

public class PushNotificationBroadcastReceiver extends BroadcastReceiver {
    private String TAG = this.getClass().getSimpleName();
    private Context context;
    private BadgeHelper badgeHelper;
    private int mNotificationId;
    private String mConversationId;
    @Inject
    GetCurrentUserUseCase getCurrentUserUseCase;
    @Inject
    ShowMissedCallNotificationUseCase showMissedCallNotificationUseCase;
    @Inject
    ShowIncomingMessageNotificationUseCase showIncomingMessageNotificationUseCase;

    private final static AtomicInteger c = new AtomicInteger(0);
    public static int getID() {
        return c.incrementAndGet();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.badgeHelper = new BadgeHelper(context);
        this.context = context;
        ((App)context.getApplicationContext()).getComponent().inject(this);
        try {
            String message = intent.getStringExtra("data");

            String conversationId = intent.getStringExtra("conversationId");
            String notificationType = intent.getStringExtra("notificationType");
            String senderProfile = intent.getStringExtra("senderProfile");
            Log.d("new message: " + message + conversationId + notificationType);
            if (TextUtils.equals(notificationType, "missed_call")) {
                int isVideo = 0;
                try {
                    isVideo = Integer.parseInt(intent.getStringExtra("isVideo"));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                String senderId = intent.getStringExtra("senderId");
                this.badgeHelper.increaseMissedCall();
                showMissedCallNotificationUseCase.execute(new DefaultObserver<>()
                        , new ShowMissedCallNotificationUseCase.Params(senderId, senderProfile, message, isVideo == 1));
            } else if (TextUtils.equals(notificationType, "incoming_message")) {
                if (!needDisplayNotification(conversationId)) {
                    return;
                }
                this.badgeHelper.increaseBadgeCount(conversationId);
                showIncomingMessageNotificationUseCase.execute(new DefaultObserver<>(),
                        new ShowIncomingMessageNotificationUseCase.Params(message, conversationId, senderProfile));
            } else if (TextUtils.equals(notificationType, "missed_call")
                    || TextUtils.equals(notificationType, "game_status")) {
                Log.d("incoming message");
                if (!needDisplayNotification(conversationId)) {
                    return;
                }
                if (TextUtils.equals(notificationType, "incoming_message")) {
                    this.badgeHelper.increaseBadgeCount(conversationId);
                } else if (TextUtils.equals(notificationType, "missed_call")) {
                    this.badgeHelper.increaseMissedCall();
                }
                boolean allowReply = notificationType.equals("incoming_message");
                getCurrentUserUseCase.execute(new DefaultObserver<User>() {
                    @Override
                    public void onNext(User user) {
                        try {
                            postNotification(context, user, message, conversationId, senderProfile, allowReply);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, null);
            } else if (TextUtils.equals(notificationType, "incoming_call")) {
                Log.d("incoming call");
                /*if (ActivityLifecycle.getInstance().isForeground()) {
                    Log.d("app in fore ground, no need to do any thing");
                    return;
                }*/
                Integer qbId = SharedPrefsHelper.getInstance().get("quickbloxId");
                String pingId = SharedPrefsHelper.getInstance().get("pingId");
                CallService.start(context, qbId, pingId);
            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    private void postNotification(Context context, User currentUser, String message,
                                  String conversationId, String profileImage, boolean allowReply) throws JSONException {
        boolean soundNotification = currentUser == null || currentUser.settings.notification;
        mNotificationId = getID();
        mConversationId = conversationId;

        // 3. Build notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        // Create pending intent, mention the Activity which needs to be
        Intent intent = new Intent(context, SplashActivity.class);
        intent.putExtra(ChatActivity.CONVERSATION_ID, conversationId);
        intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder.
                setContentText(message).
                setContentIntent(contentIntent).
                //setShowWhen(true).
                //setWhen(0).
                setAutoCancel(true);
        if (ActivityLifecycle.getInstance().isForeground() && !soundNotification) {
            notificationBuilder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE);
        } else {
            notificationBuilder.setDefaults(Notification.DEFAULT_ALL);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(NotificationManager.IMPORTANCE_HIGH);
        } else {
            notificationBuilder
                    .setPriority(Notification.PRIORITY_HIGH);
        }
        if (allowReply) {
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


                Intent intent1 = NotificationBroadcastReceiver.getReplyMessageIntent(context, mNotificationId, mConversationId);
                PendingIntent.getBroadcast(context, 100, intent1,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                // 2. Build action
                NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(
                        android.R.drawable.ic_menu_send, replyLabel, PendingIntent.getBroadcast(context, 100, intent1,
                        PendingIntent.FLAG_UPDATE_CURRENT))
                        .addRemoteInput(remoteInput)
                        .setAllowGeneratedReplies(true)
                        .build();
                notificationBuilder.addAction(replyAction);

            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            prepareProfileImage(context, profileImage, new Callback() {
                @Override
                public void complete(Object error, Object... data) {
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
                    sendNotification(notificationBuilder);
                }
            });
        } else {
            notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
            sendNotification(notificationBuilder);
        }

    }

    private void sendNotification(NotificationCompat.Builder builder) {
        NotificationManager notificationManager = ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    notificationManager.getNotificationChannel("channel0");
            if (channel == null) {
                channel = new NotificationChannel("channel0", "channel0", NotificationManager.IMPORTANCE_HIGH);
                channel.enableLights(true);
                channel.setLightColor(Color.GREEN);
                channel.enableVibration(true);
                channel.setShowBadge(true);
                notificationManager.createNotificationChannel(channel);
            }
            builder.setChannelId("channel0");
        }

        notificationManager.notify(mNotificationId, builder.build());
    }

    private void prepareProfileImage(Context context, String profileImage, Callback callback) {
        if (!profileImage.isEmpty() && profileImage.startsWith("gs://")) {
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

    private boolean needDisplayNotification(String conversationId) {
        Activity activeActivity = ActivityLifecycle.getInstance().getForegroundActivity();
        boolean isForeground = ActivityLifecycle.getInstance().isForeground();
        // do not display notification if opponentUser already logged out
        // Note: use this params to detect in case of user reinstall app
        if (!SharedPrefsHelper.getInstance().get("isLoggedIn", false)) {
            Log.d("opponentUser not logged-in");
            return false;
        }
        //do not display notification if opponentUser is opening same conversation
        if (activeActivity != null && activeActivity instanceof ChatActivity && isForeground) {
            ChatActivity chatActivity = (ChatActivity) activeActivity;
            return !conversationId.equals(chatActivity.getConversationId());
        }
        return true;
    }
}

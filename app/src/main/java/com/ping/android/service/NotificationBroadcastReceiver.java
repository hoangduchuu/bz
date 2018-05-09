package com.ping.android.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.text.TextUtils;
import android.util.Pair;
import android.widget.Toast;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.App;
import com.ping.android.R;
import com.ping.android.device.impl.NotificationImpl;
import com.ping.android.domain.usecase.CallbackUseCase;
import com.ping.android.domain.usecase.notification.ReplyMessageFromNotificationUseCase;
import com.ping.android.model.User;
import com.ping.android.presentation.view.activity.CallActivity;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * Created by bzzz on 2/28/18.
 */

public class NotificationBroadcastReceiver extends BroadcastReceiver {
    private static String REPLY_ACTION = "com.ping.android.service.NotificationHelper.REPLY_ACTION";
    private static String CALLBACK_ACTION = "com.ping.android.service.NotificationHelper.CALLBACK_ACTION";
    private static String KEY_NOTIFICATION_ID = "key_notification_id";
    private static String KEY_MESSAGE_ID = "key_message_id";
    private static String KEY_OPPONENT_USER_ID = "KEY_OPPONENT_USER_ID";
    private static String KEY_IS_VIDEO_CALL = "KEY_IS_VIDEO_CALL";
    public static String KEY_REPLY = "key_reply_message";

    @Inject
    ReplyMessageFromNotificationUseCase replyMessageFromNotificationUseCase;
    @Inject
    CallbackUseCase callbackUseCase;

    public static Intent getReplyMessageIntent(Context context, int notificationId, String messageId) {
        Intent intent = new Intent(context, NotificationBroadcastReceiver.class);
        intent.setAction(REPLY_ACTION);
        intent.putExtra(KEY_NOTIFICATION_ID, notificationId);
        intent.putExtra(KEY_MESSAGE_ID, messageId);
        return intent;
    }

    public static Intent getCallbackIntent(Context context, String opponentUserId, boolean isVideo) {
        Intent intent = new Intent(context, NotificationBroadcastReceiver.class);
        intent.setAction(CALLBACK_ACTION);
        intent.putExtra(KEY_OPPONENT_USER_ID, opponentUserId);
        intent.putExtra(KEY_IS_VIDEO_CALL, isVideo);
        return intent;
    }

    public NotificationBroadcastReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ((App)context.getApplicationContext()).getComponent().inject(this);
        if (REPLY_ACTION.equals(intent.getAction())) {
            CharSequence message = getReplyMessage(intent);
            String messageId = intent.getStringExtra(KEY_MESSAGE_ID);
            int notifyId = intent.getIntExtra(KEY_NOTIFICATION_ID, 1);
            replyMessageFromNotificationUseCase.execute(new DefaultObserver<Boolean>() {
                @Override
                public void onNext(Boolean aBoolean) {
                    updateNotification(context, notifyId);
                }

                @Override
                public void onError(@NotNull Throwable exception) {
                    exception.printStackTrace();
                    updateNotification(context, notifyId);
                }
            }, new ReplyMessageFromNotificationUseCase.Params(message.toString(), messageId));
        } else if (CALLBACK_ACTION.equals(intent.getAction())) {
            boolean isVideo = intent.getBooleanExtra(KEY_IS_VIDEO_CALL, false);
            String opponentUser = intent.getStringExtra(KEY_OPPONENT_USER_ID);
            if (!TextUtils.isEmpty(opponentUser)) {
                context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
                callbackUseCase.execute(new DefaultObserver<Pair<User, User>>() {
                    @Override
                    public void onNext(Pair<User, User> userUserPair) {
                        CallActivity.start(context, userUserPair.first, userUserPair.second, isVideo);
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                        notificationManager.cancel(userUserPair.second.key, NotificationImpl.ONGOING_NOTIFICATION_ID);
                    }
                }, opponentUser);
            } else {
                Toast.makeText(context, "Error when calling back", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateNotification(Context context, int notifyId) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "message")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentText(context.getString(R.string.notif_content_sent))
                .setTimeoutAfter(60000); // Dismiss after 1min

        notificationManager.notify(notifyId, builder.build());
    }

    public CharSequence getReplyMessage(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence(KEY_REPLY);
        }
        return null;
    }
}

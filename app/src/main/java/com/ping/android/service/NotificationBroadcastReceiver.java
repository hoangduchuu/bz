package com.ping.android.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.App;
import com.ping.android.activity.R;
import com.ping.android.domain.usecase.notification.ReplyMessageFromNotificationUseCase;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * Created by bzzz on 2/28/18.
 */

public class NotificationBroadcastReceiver extends BroadcastReceiver {
    private static String REPLY_ACTION = "com.ping.android.service.NotificationHelper.REPLY_ACTION";
    private static String KEY_NOTIFICATION_ID = "key_notification_id";
    private static String KEY_MESSAGE_ID = "key_message_id";
    public static String KEY_REPLY = "key_reply_message";

    @Inject
    ReplyMessageFromNotificationUseCase replyMessageFromNotificationUseCase;

    public static Intent getReplyMessageIntent(Context context, int notificationId, String messageId) {
        Intent intent = new Intent(context, NotificationBroadcastReceiver.class);
        intent.setAction(REPLY_ACTION);
        intent.putExtra(KEY_NOTIFICATION_ID, notificationId);
        intent.putExtra(KEY_MESSAGE_ID, messageId);
        return intent;
    }

    public NotificationBroadcastReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!REPLY_ACTION.equals(intent.getAction())) {
            return;
        }
        ((App)context.getApplicationContext()).getComponent().inject(this);
        CharSequence message = getReplyMessage(intent);
        String messageId = intent.getStringExtra(KEY_MESSAGE_ID);
        replyMessageFromNotificationUseCase.execute(new DefaultObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                int notifyId = intent.getIntExtra(KEY_NOTIFICATION_ID, 1);
                updateNotification(context, notifyId);
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
            }
        }, new ReplyMessageFromNotificationUseCase.Params(message.toString(), messageId));
    }

    private void updateNotification(Context context, int notifyId) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentText(context.getString(R.string.notif_content_sent));

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

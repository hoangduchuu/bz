package com.ping.android.service;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.ping.android.App;
import com.ping.android.activity.ChatActivity;
import com.ping.android.activity.LoadingActivity;
import com.ping.android.activity.MainActivity;
import com.ping.android.activity.R;

import org.apache.commons.lang3.StringUtils;
import org.jivesoftware.smack.chat.Chat;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Tung Tran on 12/2/2017.
 */

public class NotificationBroadcastReceiver extends BroadcastReceiver {
    private String TAG = this.getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            JSONObject notification = new JSONObject( intent.getStringExtra("notification"));
            String conversationId = intent.getStringExtra("conversationId");
            if (!needDisplayNotification(conversationId)){
                return;
            }
            postNotification(notification, conversationId, context);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException ex){
            ex.printStackTrace();
        }
    }

    private void postNotification(JSONObject notification, String conversationId, Context context) throws JSONException {

        String title = notification.getString("title");
        String body = notification.getString("body");
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        // Create pending intent, mention the Activity which needs to be
        Intent intent = new Intent(context, LoadingActivity.class);
        intent.putExtra(ChatActivity.CONVERSATION_ID, conversationId);
        intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder.
                setContentText(body).
                setContentIntent(contentIntent).
                setAutoCancel(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(NotificationManager.IMPORTANCE_HIGH);
        }else{
            notificationBuilder
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setDefaults(Notification.DEFAULT_ALL);
        }
        //do not show double BZZZ, will change if use title for other meaning
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N){
            notificationBuilder
                    .setContentTitle(title);
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            notificationBuilder.
                    setSmallIcon(R.drawable.ic_notification).
                    setColor(context.getResources().getColor(R.color.colorAccent)).
                    setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
        }else{
            notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        }
        NotificationManager notificationManager = ((NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel =
                notificationManager.getNotificationChannel("channel0");
            if (channel == null){
                channel = new NotificationChannel("channel0", "channel0", NotificationManager.IMPORTANCE_HIGH);
                channel.enableLights(true);
                channel.setLightColor(Color.GREEN);
                channel.enableVibration(true);
                notificationManager.createNotificationChannel(channel);
            }
            notificationBuilder.setChannelId("channel0");
        }

        notificationManager.notify(0, notificationBuilder.build());

    }

    private boolean needDisplayNotification(String conversationId){
        Activity activeActivity = App.getActiveActivity();
        if (activeActivity != null && activeActivity instanceof ChatActivity){
            ChatActivity chatActivity = (ChatActivity) activeActivity;

            if (StringUtils.equals(chatActivity.getConversationId(), conversationId)){
                return false;
            }
        }
        return true;
    }
}

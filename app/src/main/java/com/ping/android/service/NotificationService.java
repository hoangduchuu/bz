package com.ping.android.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ping.android.activity.ChatActivity;
import com.ping.android.activity.R;
import com.ping.android.model.Conversation;
import com.ping.android.model.User;
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.Constant;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;

public class NotificationService extends Service {

    private final String TAG = "Ping: " + this.getClass().getSimpleName();

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mConversationRef;
    private ChildEventListener mConversationEvent;

    private User currentUser;
    private ArrayList<Conversation> conversations;
    private String currentConservationID = "";

    /**
     * Called when the service is being created.
     */
    @Override
    public void onCreate() {
        conversations = new ArrayList<>();
        mConversationEvent = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.i(TAG, String.format("users.%s addChildEventListener onChildAdded key: %s",
                        mAuth.getCurrentUser().getUid(), "conversations", dataSnapshot.getKey()));
                Conversation conversation = new Conversation(dataSnapshot);
                conversation.key = dataSnapshot.getKey();
                if (MapUtils.isEmpty(conversation.memberIDs)) {
                    return;
                }
                ServiceManager.getInstance().initMembers(conversation.memberIDs, new Callback() {
                    @Override
                    public void complete(Object error, Object... data) {
                        conversation.members = (List<User>) data[0];
                        conversations.add(conversation);
                        showNewMessage(conversation);
                        updateShortcutBadger();
                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.i(TAG, String.format("users.%s addChildEventListener onChildChanged key: %s",
                        mAuth.getCurrentUser().getUid(), "conversations", dataSnapshot.getKey()));
                Conversation conversation = new Conversation(dataSnapshot);
                conversation.key = dataSnapshot.getKey();
                ServiceManager.getInstance().initMembers(conversation.memberIDs, new Callback() {
                    @Override
                    public void complete(Object error, Object... data) {
                        conversation.members = (List<User>) data[0];
                    }
                });
                for (int i = 0; i < conversations.size(); i++) {
                    if (conversations.get(i).key.equals(conversation.key)) {
                        if(StringUtils.isEmpty(conversations.get(i).message)){
                            break;
                        }
                        if (!conversations.get(i).message.equals(conversation.message) ||
                                !conversations.get(i).timesstamps.equals(conversation.timesstamps)) {
                            showNewMessage(conversation);
                        }
                        conversations.set(i, conversation);
                        break;
                    }
                }
                updateShortcutBadger();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

    }

    /**
     * The service is starting, due to a call to startService()
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("CONVERSATION_ID")) {
            currentConservationID = intent.getStringExtra("CONVERSATION_ID");
        }
        if (intent != null && intent.hasExtra("OBSERVE_FLAG")) {
            Boolean observeFlg = intent.getBooleanExtra("OBSERVE_FLAG", false);
            observeConversation(observeFlg);
        }
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Called when all clients have unbound with unbindService()
     */
    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    /**
     * Called when a client is binding to the service with bindService()
     */
    @Override
    public void onRebind(Intent intent) {

    }

    /**
     * Called when The service is no longer used and is being destroyed
     */
    @Override
    public void onDestroy() {

    }

    private void observeConversation(Boolean observe) {
        if (observe) {
            FirebaseUser user = mAuth.getCurrentUser();
            if (mConversationRef != null) {
                mConversationRef.removeEventListener(mConversationEvent);
                mConversationRef = null;
                conversations = new ArrayList<>();
            }
            if (user != null) {
                conversations = new ArrayList<>();
                // User is signed in
                ServiceManager.getInstance().initUserData(new Callback() {
                    @Override
                    public void complete(Object error, Object... data) {
                        currentUser = ServiceManager.getInstance().getCurrentUser();
                        mConversationRef = database.getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("conversations");
                        mConversationRef.addChildEventListener(mConversationEvent);
                    }
                });
            }
        } else {
            if (mConversationRef != null) {
                mConversationRef.removeEventListener(mConversationEvent);
                mConversationRef = null;
                conversations = new ArrayList<>();
            }
            currentUser = null;
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(Constant.NOTIFICATION_NEW_MSG);
        }
    }

    private void showNewMessage(Conversation conversation) {
        if (currentUser != null && !currentUser.settings.notification) {
            return;
        }
        if (ServiceManager.getInstance().getCurrentReadStatus(conversation.readStatuses)) {
            return;
        }
        if (StringUtils.isEmpty(conversation.senderId) || conversation.senderId.equals(currentUser.key)) {
            return;
        }
        if (currentConservationID.equals(conversation.key)) {
            return;
        }

        ServiceManager.getInstance().initMembers(conversation.memberIDs, new Callback() {
            @Override
            public void complete(Object error, Object... data) {
                conversation.members = (List<User>) data[0];
                String sender = "", message = "";
                for (User user : conversation.members) {
                    if (!user.key.equals(currentUser.key)) {
                        sender = user.getDisplayName();
                        break;
                    }
                }

                if (conversation.messageType == Constant.MSG_TYPE_TEXT) {
                    if (ServiceManager.getInstance().getCurrentMarkStatus(conversation.markStatuses, conversation.maskMessages)) {
                        message = ServiceManager.getInstance().encodeMessage(getApplicationContext(), conversation.message);
                    } else {
                        message = conversation.message;
                    }

                } else if (conversation.messageType == Constant.MSG_TYPE_IMAGE) {
                    message = "[Picture]";
                } else if (conversation.messageType == Constant.MSG_TYPE_VOICE) {
                    message = "[Voice]";
                } else if (conversation.messageType == Constant.MSG_TYPE_GAME) {
                    message = "[Game]";
                }

                Intent showTaskIntent = new Intent(getApplicationContext(), ChatActivity.class);
                showTaskIntent.putExtra("CONVERSATION_ID", conversation.key);
                showTaskIntent.setAction(Intent.ACTION_MAIN);
                showTaskIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                showTaskIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                PendingIntent contentIntent = PendingIntent.getActivity(
                        getApplicationContext(),
                        0,
                        showTaskIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(getApplicationContext())
                                .setSmallIcon(R.drawable.ic_tab_message_on)
                                .setLargeIcon(BitmapFactory.decodeResource(getBaseContext().getResources(),
                                        R.mipmap.ic_launcher))
                                .setContentTitle(sender)
                                .setColor(Color.parseColor("#FFFBB040"))
                                .setContentText(message)
                                .setContentIntent(contentIntent)
                                .setAutoCancel(true)
                                .setDefaults(Notification.DEFAULT_ALL)
                                .setPriority(Notification.PRIORITY_HIGH);
                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                notificationManager.notify(Constant.NOTIFICATION_NEW_MSG, mBuilder.build());
            }
        });


    }

    private void updateShortcutBadger() {
        int badgeCount = 0;
        for (Conversation conversation : conversations) {
            if (conversation.readStatuses == null || (conversation.readStatuses.containsKey(currentUser.key) &&
                    conversation.readStatuses.get(currentUser.key))) {
                continue;
            }
            if (StringUtils.isEmpty(conversation.senderId) || conversation.senderId.equals(currentUser.key)) {
                continue;
            }
            if (currentConservationID.equals(conversation.key)) {
                continue;
            }
            badgeCount++;
        }
        if (badgeCount > 0) {
            ShortcutBadger.applyCount(this, badgeCount);
        } else {
            ShortcutBadger.applyCount(this, 0);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(Constant.PREFS_KEY_MESSAGE_COUNT, badgeCount);
        editor.apply();
    }
}

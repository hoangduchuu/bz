package com.ping.android.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ping.android.App;
import com.ping.android.ultility.Consts;
import com.ping.android.managers.ChatPingAlarmManager;
import com.ping.android.utils.SettingsUtil;
import com.ping.android.utils.WebRtcSessionManager;
import com.ping.android.utils.bus.BusProvider;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.QBWebRTCSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.messages.services.SubscribeService;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCConfig;

import org.jivesoftware.smackx.ping.PingFailedListener;

import javax.inject.Inject;

/**
 * QuickBlox team
 */
public class CallService extends Service {
    private static final String TAG = CallService.class.getSimpleName();

    public static final String ACTION_LOG_IN = "com.ping.android.ACTION_LOG_IN";
    public static final String ACTION_LOG_OUT = "com.ping.android.ACTION_LOG_OUT";

    public static final String EXTRA_QB_ID = "EXTRA_QB_ID";
    public static final String EXTRA_PING_ID = "EXTRA_PING_ID";

    private QBChatService chatService;
    private QBRTCClient rtcClient;
    private PendingIntent pendingIntent;
    private int currentCommand;
    private QBUser currentUser;

    @Inject
    CallServiceHandler handler;

    public static void start(Context context, QBUser qbUser, PendingIntent pendingIntent) {
        Intent intent = new Intent(context, CallService.class);
        Log.d(TAG, "going to start call service");
        intent.putExtra(EXTRA_QB_ID, qbUser.getId());
        intent.putExtra(EXTRA_PING_ID, qbUser.getLogin());
        intent.setAction(ACTION_LOG_IN);
        //intent.putExtra(Consts.EXTRA_PENDING_INTENT, pendingIntent);
        context.startService(intent);
    }

    public static void start(Context context, QBUser qbUser) {
        start(context, qbUser, null);
    }

    public static void logout(Context context) {
        Intent intent = new Intent(context, CallService.class);
        intent.putExtra(Consts.EXTRA_COMMAND_TO_SERVICE, Consts.COMMAND_LOGOUT);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ((App) getApplication()).getComponent().inject(this);
        initChatService();

        Log.d(TAG, "Service onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        if (intent != null && intent.getAction() != null) {
            if (ACTION_LOG_IN.equals(intent.getAction())) {
                logUserIn(intent);
            }
            if (ACTION_LOG_OUT.equals(intent.getAction())) {
                logUserOut(intent);
            }
        }
//        parseIntentExtras(intent);
//
//        startSuitableActions();

        return START_STICKY;
    }

    private void logUserOut(Intent intent) {

    }

    private void logUserIn(Intent intent) {
        String pingId = intent.getStringExtra(EXTRA_PING_ID);
        int qbId = intent.getIntExtra(EXTRA_QB_ID, -1);
        if (qbId > 0) {
            handler.loginUser(qbId, pingId);
        }
    }

    private void parseIntentExtras(Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            currentCommand = intent.getIntExtra(Consts.EXTRA_COMMAND_TO_SERVICE, Consts.COMMAND_NOT_FOUND);
            pendingIntent = intent.getParcelableExtra(Consts.EXTRA_PENDING_INTENT);
            currentUser = (QBUser) intent.getSerializableExtra(Consts.EXTRA_QB_USER);
        }
    }

    private void startSuitableActions() {
        if (currentCommand == Consts.COMMAND_LOGIN) {
            startLoginToChat();
        } else if (currentCommand == Consts.COMMAND_LOGOUT) {
            logout();
        }
    }

    private void initChatService() {
        QBChatService.setDebugEnabled(true);
    }

    private void startLoginToChat() {
        if (currentUser == null || currentUser.getId() == null) return;
        if (!chatService.isLoggedIn()) {
            loginToChat(currentUser);
        } else {
            sendResultToActivity(true, null);
        }
    }

    private void loginToChat(QBUser qbUser) {
        chatService.login(qbUser, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                Log.d(TAG, "login onSuccess");
                startActionsOnSuccessLogin();
            }

            @Override
            public void onError(QBResponseException e) {
                Log.d(TAG, "login onError " + e.getMessage());
                sendResultToActivity(false, e.getMessage() != null
                        ? e.getMessage()
                        : "Login error");
            }
        });
    }

    private void startActionsOnSuccessLogin() {
        initPingListener();
        initQBRTCClient();
        SubscribeService.subscribeToPushes(getApplicationContext(), false);
        sendResultToActivity(true, null);
    }

    private void initPingListener() {
        ChatPingAlarmManager.onCreate(this);
        ChatPingAlarmManager.addPingListener(new PingFailedListener() {
            @Override
            public void pingFailed() {
                Log.d(TAG, "Ping chat server failed");
            }
        });
    }

    private void initQBRTCClient() {
        rtcClient = QBRTCClient.getInstance(getApplicationContext());

        // Add signalling manager
        // FIXME: issue https://github.com/QuickBlox/quickblox-android-sdk/issues/441
        chatService.getVideoChatWebRTCSignalingManager().addSignalingManagerListener(new QBVideoChatSignalingManagerListener() {
            @Override
            public void signalingCreated(QBSignaling qbSignaling, boolean createdLocally) {
                if (!createdLocally) {
                    rtcClient.addSignaling((QBWebRTCSignaling) qbSignaling);
                }
            }
        });

        // Configure
        QBRTCConfig.setDebugEnabled(true);
        SettingsUtil.configRTCTimers(this);

        // Add service as callback to RTCClient
        rtcClient.addSessionCallbacksListener(WebRtcSessionManager.getInstance());
        rtcClient.prepareToProcessCalls();
    }

    private void sendResultToActivity(boolean isSuccess, String errorMessage) {
        if (pendingIntent != null) {
            Log.d(TAG, "sendResultToActivity()");
            try {
                Intent intent = new Intent();
                intent.putExtra(Consts.EXTRA_LOGIN_RESULT, isSuccess);
                intent.putExtra(Consts.EXTRA_LOGIN_ERROR_MESSAGE, errorMessage);

                pendingIntent.send(CallService.this, Consts.EXTRA_LOGIN_RESULT_CODE, intent);
            } catch (PendingIntent.CanceledException e) {
                String errorMessageSendingResult = e.getMessage();
                Log.d(TAG, errorMessageSendingResult != null
                        ? errorMessageSendingResult
                        : "Error sending result to activity");
            }
        }
    }

    private void logout() {
        destroyRtcClientAndChat();
    }

    private void destroyRtcClientAndChat() {
        if (rtcClient != null) {
            rtcClient.destroy();
        }
        //TODO ChatPingAlarmManager.onDestroy();
        if (chatService != null) {
            chatService.logout(new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid, Bundle bundle) {
                    chatService.destroy();
                }

                @Override
                public void onError(QBResponseException e) {
                    Log.d(TAG, "logout onError " + e.getMessage());
                    chatService.destroy();
                }
            });
        }
        stopSelf();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service onDestroy()");
        super.onDestroy();
        handler.destroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Service onBind)");
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "Service onTaskRemoved()");
        super.onTaskRemoved(rootIntent);
        destroyRtcClientAndChat();
    }
}

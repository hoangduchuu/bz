package com.ping.android.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ping.android.App;
import com.ping.android.db.QbUsersDbManager;
import com.ping.android.fragment.AudioConversationFragment;
import com.ping.android.fragment.BaseConversationFragment;
import com.ping.android.fragment.ConversationFragmentCallbackListener;
import com.ping.android.fragment.IncomeCallFragment;
import com.ping.android.fragment.IncomeCallFragmentCallbackListener;
import com.ping.android.fragment.OnCallEventsController;
import com.ping.android.fragment.VideoConversationFragment;
import com.ping.android.model.Call;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.Constant;
import com.ping.android.ultility.Consts;
import com.ping.android.util.NetworkConnectionChecker;
import com.ping.android.utils.FragmentExecuotr;
import com.ping.android.utils.PermissionsChecker;
import com.ping.android.utils.RingtonePlayer;
import com.ping.android.utils.SettingsUtil;
import com.ping.android.utils.Toaster;
import com.ping.android.utils.UsersUtils;
import com.ping.android.utils.WebRtcSessionManager;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.AppRTCAudioManager;
import com.quickblox.videochat.webrtc.QBRTCCameraVideoCapturer;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCScreenCapturer;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.QBSignalingSpec;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionEventsCallback;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSignalingCallback;
import com.quickblox.videochat.webrtc.exception.QBRTCSignalException;

import org.jivesoftware.smack.AbstractConnectionListener;
import org.webrtc.CameraVideoCapturer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * QuickBlox team
 */
public class CallActivity extends BaseActivity implements QBRTCClientSessionCallbacks, QBRTCSessionStateCallback, QBRTCSignalingCallback, View.OnClickListener,
        OnCallEventsController, IncomeCallFragmentCallbackListener, ConversationFragmentCallbackListener, NetworkConnectionChecker.OnConnectivityChangedListener {

    public static final String OPPONENTS_CALL_FRAGMENT = "opponents_call_fragment";
    public static final String INCOME_CALL_FRAGMENT = "income_call_fragment";
    public static final String CONVERSATION_CALL_FRAGMENT = "conversation_call_fragment";
    public static final String CALLER_NAME = "caller_name";
    public static final String SESSION_ID = "sessionID";
    public static final String START_CONVERSATION_REASON = "start_conversation_reason";
    private static final String TAG = CallActivity.class.getSimpleName();
    private static final int REQUEST_MEDIA_PROJECTION = 1;
    public List<QBUser> opponentsList;
    private QBRTCSession currentSession;
    private Runnable showIncomingCallWindowTask;
    private Handler showIncomingCallWindowTaskHandler;
    private boolean closeByWifiStateAllow = true;
    private RingtonePlayer ringtonePlayer;
    private String hangUpReason;
    private boolean isInCommingCall;
    private QBRTCClient rtcClient;
    private OnChangeDynamicToggle onChangeDynamicCallback;
    private ConnectionListener connectionListener;
    private boolean wifiEnabled = true;
    private SharedPreferences sharedPref;
    //private LinearLayout connectionView;
    private AppRTCAudioManager audioManager;
    private NetworkConnectionChecker networkConnectionChecker;
    private WebRtcSessionManager sessionManager;
    private QbUsersDbManager dbManager;
    private ArrayList<CurrentCallStateCallback> currentCallStateCallbackList = new ArrayList<>();
    private List<Integer> opponentsIdsList;
    private boolean callStarted;
    private boolean isVideoCall;
    private long expirationReconnectionTime;
    private int reconnectHangUpTimeMillis;
    private boolean headsetPlugged;
    private boolean previousDeviceEarPiece;
    private boolean showToastAfterHeadsetPlugged = true;
    private PermissionsChecker checker;
    private MediaProjectionManager mMediaProjectionManager;

    private FirebaseDatabase database;
    private DatabaseReference mDatabase;

    private Call callHistory;
    private User currentUser;
    private User otherUser;

    private boolean isSendHistory = false;

    public static void start(Context context, User otherUser, Boolean isVideoCall) {
        Long userQBID = otherUser.quickBloxID;
        User currentUser = ServiceManager.getInstance().getCurrentUser();

        if (!ServiceManager.getInstance().getNetworkStatus(context)) {
            Toast.makeText(context, "Please check network connection", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ServiceManager.getInstance().isBlockBy(otherUser)) {
            String username = ServiceManager.getInstance().getFirstName(otherUser);
            Toaster.shortToast(String.format(context.getString(R.string.msg_account_blocked_by), username));
            return;
        }

        if (ServiceManager.getInstance().isBlock(otherUser.key)) {
            String username = ServiceManager.getInstance().getFirstName(otherUser);
            Toaster.shortToast(String.format(context.getString(R.string.msg_account_call_blocked), username, username));
            return;
        }

        if (currentUser.quickBloxID == null || currentUser.quickBloxID == 0) {
            Toaster.shortToast(context.getString(R.string.msg_current_user_empty_quickbloxID));
            return;
        }

        if (userQBID == null || userQBID == 0) {
            Toaster.shortToast(context.getString(R.string.msg_opponent_user_empty_quickbloxID));
            return;
        }
        //QBUser opponentQBUser = ServiceManager.getInstance().getQBUserByPingID(userID);
        ArrayList<Integer> opponentsList = new ArrayList<>();
        opponentsList.add(userQBID.intValue());
        if (opponentsList.size() == 0) {
            return;
        }

        //TODO CollectionsUtils.getIdsSelectedOpponents(opponentsAdapter.getSelectedItems());
        QBRTCTypes.QBConferenceType conferenceType = isVideoCall
                ? QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO
                : QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;

        QBRTCClient qbrtcClient = QBRTCClient.getInstance(App.getInstance().getApplicationContext());

        QBRTCSession newQbRtcSession = qbrtcClient.createNewSessionWithOpponents(opponentsList, conferenceType);

        WebRtcSessionManager.getInstance(context).setCurrentSession(newQbRtcSession);

        //PushNotificationSender.sendPushMessage(opponentsList, currentUser.getFullName());

        CallActivity.start(context, false);
    }

    public static void start(Context context,
                             boolean isIncomingCall) {

        Intent intent = new Intent(context, CallActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(Consts.EXTRA_IS_INCOMING_CALL, isIncomingCall);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        parseIntentExtras();

        sessionManager = WebRtcSessionManager.getInstance(this);
        if (!currentSessionExist()) {
//            we have already currentSession == null, so it's no reason to do further initialization
            finish();
            Log.d(TAG, "finish CallActivity");
            return;
        }

        initFields();
        initCurrentSession(currentSession);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        initQBRTCClient();
        initAudioManager();
        initWiFiManagerListener();

        ringtonePlayer = new RingtonePlayer(this, R.raw.beep);

        //connectionView = (LinearLayout) View.inflate(this, R.layout.connection_popup, null);
        checker = new PermissionsChecker(getApplicationContext());

        startSuitableFragment(isInCommingCall);
        findViewById(R.id.call_back).setOnClickListener(this);
        initData();
    }

    private void initData() {
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();

        currentUser = ServiceManager.getInstance().getCurrentUser();
        if (isInCommingCall) {
            otherUser = ServiceManager.getInstance().getUserByQBId(currentSession.getCallerID());
        } else {
            otherUser = ServiceManager.getInstance().getUserByQBId(currentSession.getOpponents().get(0));
        }
        double timestamp = System.currentTimeMillis() / 1000L;
        callHistory = new Call(currentUser.key, otherUser.key, Constant.CALL_STATUS_SUCCESS, getCallDeleteStatuses(), timestamp);
    }

    private void insertCallHistory(int status) {
        if(isSendHistory) return;
        isSendHistory = true;
        callHistory.status = status;
        String historyKey = mDatabase.child("calls").push().getKey();
        mDatabase.child("calls").child(historyKey).setValue(callHistory.toMap());
        mDatabase.child("users").child(otherUser.key).child("calls").child(historyKey).setValue(callHistory.toMap());

        callHistory.status = Constant.CALL_STATUS_SUCCESS;
        mDatabase.child("users").child(currentUser.key).child("calls").child(historyKey).setValue(callHistory.toMap());
    }

    private Map<String, Boolean> getCallDeleteStatuses() {
        Map<String, Boolean> deleteStatuses = new HashMap<>();
        deleteStatuses.put(currentUser.key, false);
        deleteStatuses.put(otherUser.key, false);
        return deleteStatuses;
    }

    private void returnToCamera() {
        try {
            currentSession.getMediaStreamManager().setVideoCapturer(new QBRTCCameraVideoCapturer(this, null));
        } catch (QBRTCCameraVideoCapturer.QBRTCCameraCapturerException e) {
            Log.i(TAG, "Error: device doesn't have camera");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        Log.i(TAG, "onActivityResult requestCode=" + requestCode + ", resultCode= " + resultCode);
        if (requestCode == QBRTCScreenCapturer.REQUEST_MEDIA_PROJECTION) {
            if (resultCode == Activity.RESULT_OK) {
                Log.i(TAG, "Starting screen capture");
            } else {

            }
        }
    }

    private void startSuitableFragment(boolean isInComingCall) {
        if (isInComingCall) {
            initIncomingCallTask();
            startLoadAbsentUsers();
            checkPermission();
            addIncomeCallFragment();
        } else {
            checkPermission();
            addConversationFragment(isInComingCall);
        }
    }

    private void checkPermission() {
        if (checker.lacksPermissions(Consts.PERMISSIONS)) {
            startPermissionsActivity(!isVideoCall);
        }
    }

    private void startPermissionsActivity(boolean checkOnlyAudio) {
        PermissionsActivity.startActivity(this, checkOnlyAudio, Consts.PERMISSIONS);
    }

    private void startLoadAbsentUsers() {
        ArrayList<QBUser> usersFromDb = dbManager.getAllUsers();
        ArrayList<Integer> allParticipantsOfCall = new ArrayList<>();
        allParticipantsOfCall.addAll(opponentsIdsList);

        if (isInCommingCall) {
            allParticipantsOfCall.add(currentSession.getCallerID());
        }

        ArrayList<Integer> idsUsersNeedLoad = UsersUtils.getIdsNotLoadedUsers(usersFromDb, allParticipantsOfCall);
        if (!idsUsersNeedLoad.isEmpty()) {
            requestExecutor.loadUsersByIds(idsUsersNeedLoad, new QBEntityCallbackImpl<ArrayList<QBUser>>() {
                @Override
                public void onSuccess(ArrayList<QBUser> result, Bundle params) {
                    dbManager.saveAllUsers(result, false);
                    needUpdateOpponentsList(result);
                }
            });
        }
    }

    private void needUpdateOpponentsList(ArrayList<QBUser> newUsers) {
        notifyCallStateListenersNeedUpdateOpponentsList(newUsers);
    }

    private boolean currentSessionExist() {
        currentSession = sessionManager.getCurrentSession();
        return currentSession != null;
    }

    private void initFields() {
        dbManager = QbUsersDbManager.getInstance(getApplicationContext());
        opponentsIdsList = currentSession.getOpponents();
    }

    @Override
    protected View getSnackbarAnchorView() {
        return null;
    }

    private void parseIntentExtras() {
        isInCommingCall = getIntent().getExtras().getBoolean(Consts.EXTRA_IS_INCOMING_CALL);
    }

    private void initAudioManager() {
        audioManager = AppRTCAudioManager.create(this, new AppRTCAudioManager.OnAudioManagerStateListener() {
            @Override
            public void onAudioChangedState(AppRTCAudioManager.AudioDevice audioDevice) {
                if (callStarted) {
                    if (audioManager.getSelectedAudioDevice() == AppRTCAudioManager.AudioDevice.EARPIECE) {
                        previousDeviceEarPiece = true;
                    } else if (audioManager.getSelectedAudioDevice() == AppRTCAudioManager.AudioDevice.SPEAKER_PHONE) {
                        previousDeviceEarPiece = false;
                    }
                    if (showToastAfterHeadsetPlugged) {
                        //Toaster.shortToast("Audio device switched to  " + audioDevice);
                    }
                }
            }
        });

        isVideoCall = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO.equals(currentSession.getConferenceType());
        if (isVideoCall) {
            audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
            Log.d(TAG, "AppRTCAudioManager.AudioDevice.SPEAKER_PHONE");
        } else {
            audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
            previousDeviceEarPiece = true;
            Log.d(TAG, "AppRTCAudioManager.AudioDevice.EARPIECE");
        }

        audioManager.setOnWiredHeadsetStateListener(new AppRTCAudioManager.OnWiredHeadsetStateListener() {
            @Override
            public void onWiredHeadsetStateChanged(boolean plugged, boolean hasMicrophone) {
                headsetPlugged = plugged;
                if (callStarted) {
                    //Toaster.shortToast("Headset " + (plugged ? "plugged" : "unplugged"));
                }
                if (onChangeDynamicCallback != null) {
                    if (!plugged) {
                        showToastAfterHeadsetPlugged = false;
                        if (previousDeviceEarPiece) {
                            setAudioDeviceDelayed(AppRTCAudioManager.AudioDevice.EARPIECE);
                        } else {
                            setAudioDeviceDelayed(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
                        }
                    }
                    onChangeDynamicCallback.enableDynamicToggle(plugged, previousDeviceEarPiece);
                }
            }
        });
        audioManager.init();
    }

    private void setAudioDeviceDelayed(final AppRTCAudioManager.AudioDevice audioDevice) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showToastAfterHeadsetPlugged = true;
                audioManager.setAudioDevice(audioDevice);
            }
        }, 500);
    }

    private void initQBRTCClient() {
        rtcClient = QBRTCClient.getInstance(this);

        rtcClient.setCameraErrorHendler(new CameraVideoCapturer.CameraEventsHandler() {
            @Override
            public void onCameraError(final String s) {
            }

            @Override
            public void onCameraDisconnected() {
            }

            @Override
            public void onCameraFreezed(String s) {
                hangUpCurrentSession();
            }

            @Override
            public void onCameraOpening(String s) {
            }

            @Override
            public void onFirstFrameAvailable() {
            }

            @Override
            public void onCameraClosed() {
            }
        });


        // Configure
        //
        QBRTCConfig.setMaxOpponentsCount(Consts.MAX_OPPONENTS_COUNT);
        SettingsUtil.setSettingsStrategy(opponentsIdsList, sharedPref, CallActivity.this);
        SettingsUtil.configRTCTimers(CallActivity.this);
        QBRTCConfig.setDebugEnabled(true);


        // Add activity as callback to RTCClient
        rtcClient.addSessionCallbacksListener(this);
        // Start mange QBRTCSessions according to VideoCall parser's callbacks
        rtcClient.prepareToProcessCalls();
        connectionListener = new ConnectionListener();
        QBChatService.getInstance().addConnectionListener(connectionListener);
    }

    private void setExpirationReconnectionTime() {
        reconnectHangUpTimeMillis = SettingsUtil.getPreferenceInt(sharedPref, this, R.string.pref_disconnect_time_interval_key,
                R.string.pref_disconnect_time_interval_default_value) * 1000;
        expirationReconnectionTime = System.currentTimeMillis() + reconnectHangUpTimeMillis;
    }

    private void hangUpAfterLongReconnection() {
        if (expirationReconnectionTime < System.currentTimeMillis()) {
            hangUpCurrentSession();
        }
    }

    @Override
    public void connectivityChanged(boolean availableNow) {
        if (callStarted) {
        }
    }

    @Override
    public void connectivityChanged(Constant.NETWORK_STATUS networkStatus) {

    }

    private void showNotificationPopUp(final int text, final boolean show) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (show) {
//                    ((TextView) connectionView.findViewById(R.id.notification)).setText(text);
//                    if (connectionView.getParent() == null) {
//                        ((ViewGroup) CallActivity.this.findViewById(R.id.fragment_container)).addView(connectionView);
//                    }
//                } else {
//                    ((ViewGroup) CallActivity.this.findViewById(R.id.fragment_container)).removeView(connectionView);
//                }
//            }
//        });

    }

    private void initWiFiManagerListener() {
        networkConnectionChecker = new NetworkConnectionChecker(getApplication());
    }

    private void initIncomingCallTask() {
        showIncomingCallWindowTaskHandler = new Handler(Looper.myLooper());
        showIncomingCallWindowTask = new Runnable() {
            @Override
            public void run() {
                if (currentSession == null) {
                    return;
                }

                QBRTCSession.QBRTCSessionState currentSessionState = currentSession.getState();
                if (QBRTCSession.QBRTCSessionState.QB_RTC_SESSION_NEW.equals(currentSessionState)) {
                    rejectCurrentSession();
                } else {
                    ringtonePlayer.stop();
                    hangUpCurrentSession();
                }
                //Toaster.longToast("Call was stopped by timer");
            }
        };
    }


    private QBRTCSession getCurrentSession() {
        return currentSession;
    }

    public void rejectCurrentSession() {
        if (getCurrentSession() != null) {
            getCurrentSession().rejectCall(new HashMap<String, String>());
        }
    }

    public void hangUpCurrentSession() {
        ringtonePlayer.stop();
        if (getCurrentSession() != null) {
            getCurrentSession().hangUp(new HashMap<String, String>());
        }
    }

    private void setAudioEnabled(boolean isAudioEnabled) {
        if (currentSession != null && currentSession.getMediaStreamManager() != null) {
            currentSession.getMediaStreamManager().getLocalAudioTrack().setEnabled(isAudioEnabled);
        }
    }

    private void setVideoEnabled(boolean isVideoEnabled) {
        if (currentSession != null && currentSession.getMediaStreamManager() != null) {
            currentSession.getMediaStreamManager().getLocalVideoTrack().setEnabled(isVideoEnabled);
        }
    }

    private void startIncomeCallTimer(long time) {
        showIncomingCallWindowTaskHandler.postAtTime(showIncomingCallWindowTask, SystemClock.uptimeMillis() + time);
    }

    private void stopIncomeCallTimer() {
        Log.d(TAG, "stopIncomeCallTimer");
        showIncomingCallWindowTaskHandler.removeCallbacks(showIncomingCallWindowTask);
    }


    @Override
    protected void onResume() {
        super.onResume();
        networkConnectionChecker.registerListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        networkConnectionChecker.unregisterListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        QBChatService.getInstance().removeConnectionListener(connectionListener);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.call_back:
                hangUpCurrentSession();
                break;
        }
    }

    private void forbiddenCloseByWifiState() {
        closeByWifiStateAllow = false;
    }


    public void initCurrentSession(QBRTCSession session) {
        if (session != null) {
            Log.d(TAG, "Init new QBRTCSession");
            this.currentSession = session;
            this.currentSession.addSessionCallbacksListener(CallActivity.this);
            this.currentSession.addSignalingCallback(CallActivity.this);
        }
    }

    public void releaseCurrentSession() {
        Log.d(TAG, "Release current session");
        if (currentSession != null) {
            this.currentSession.removeSessionCallbacksListener(CallActivity.this);
            this.currentSession.removeSignalingCallback(CallActivity.this);
            rtcClient.removeSessionsCallbacksListener(CallActivity.this);
            this.currentSession = null;
        }
    }

    // ---------------Chat callback methods implementation  ----------------------//

    @Override
    public void onReceiveNewSession(final QBRTCSession session) {
        Log.d(TAG, "Session " + session.getSessionID() + " are income");
        if (getCurrentSession() != null) {
            Log.d(TAG, "Stop new session. Device now is busy");
            session.rejectCall(null);
        }
    }

    @Override
    public void onUserNotAnswer(QBRTCSession session, Integer userID) {
        if (!session.equals(getCurrentSession())) {
            return;
        }
        insertCallHistory(Constant.CALL_STATUS_MISS);
        ringtonePlayer.stop();
    }

    @Override
    public void onUserNoActions(QBRTCSession qbrtcSession, Integer integer) {
        startIncomeCallTimer(0);
    }

    @Override
    public void onCallAcceptByUser(QBRTCSession session, Integer userId, Map<String, String> userInfo) {
        if (!session.equals(getCurrentSession())) {
            return;
        }
        insertCallHistory(Constant.CALL_STATUS_SUCCESS);
        ringtonePlayer.stop();
    }

    @Override
    public void onCallRejectByUser(QBRTCSession session, Integer userID, Map<String, String> userInfo) {
        if (!session.equals(getCurrentSession())) {
            return;
        }
        insertCallHistory(Constant.CALL_STATUS_MISS);
        ringtonePlayer.stop();
    }

    @Override
    public void onConnectionClosedForUser(QBRTCSession session, Integer userID) {
        // Close app after session close of network was disabled
        if (hangUpReason != null && hangUpReason.equals(Consts.WIFI_DISABLED)) {
            Intent returnIntent = new Intent();
            setResult(Consts.CALL_ACTIVITY_CLOSE_WIFI_DISABLED, returnIntent);
            finish();
        }
    }

    @Override
    public void onConnectedToUser(QBRTCSession session, final Integer userID) {
        callStarted = true;
        notifyCallStateListenersCallStarted();
        forbiddenCloseByWifiState();
        if (isInCommingCall) {
            stopIncomeCallTimer();
        }
        Log.d(TAG, "onConnectedToUser() is started");
    }

    @Override
    public void onSessionClosed(final QBRTCSession session) {

        Log.d(TAG, "Session " + session.getSessionID() + " start stop session");

        if (session.equals(getCurrentSession())) {
            Log.d(TAG, "Stop session");

            if (audioManager != null) {
                audioManager.close();
            }
            releaseCurrentSession();

            closeByWifiStateAllow = true;
            finish();
        }
    }

    @Override
    public void onSessionStartClose(final QBRTCSession session) {
        if (session.equals(getCurrentSession())) {
            session.removeSessionCallbacksListener(CallActivity.this);
            notifyCallStateListenersCallStopped();
        }
    }

    @Override
    public void onDisconnectedFromUser(QBRTCSession session, Integer userID) {

    }

    private void showToast(final int message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toaster.shortToast(message);
            }
        });
    }

    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toaster.shortToast(message);
            }
        });
    }

    @Override
    public void onReceiveHangUpFromUser(final QBRTCSession session, final Integer userID, Map<String, String> map) {
        if (session.equals(getCurrentSession())) {

            if (userID.equals(session.getCallerID())) {
                hangUpCurrentSession();
                Log.d(TAG, "initiator hung up the call");
            }

            QBUser participant = dbManager.getUserById(userID);
            final String participantName = participant != null ? participant.getFullName() : String.valueOf(userID);
        }
    }

    private android.support.v4.app.Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

    private void addIncomeCallFragment() {
        Log.d(TAG, "QBRTCSession in addIncomeCallFragment is " + currentSession);

        if (currentSession != null) {
            IncomeCallFragment fragment = new IncomeCallFragment();
            FragmentExecuotr.addFragment(getSupportFragmentManager(), R.id.fragment_container, fragment, INCOME_CALL_FRAGMENT);
        } else {
            Log.d(TAG, "SKIP addIncomeCallFragment method");
        }
    }

    private void addConversationFragment(boolean isIncomingCall) {
        BaseConversationFragment conversationFragment = BaseConversationFragment.newInstance(
                isVideoCall
                        ? new VideoConversationFragment()
                        : new AudioConversationFragment(),
                isIncomingCall);
        FragmentExecuotr.addFragment(getSupportFragmentManager(), R.id.fragment_container, conversationFragment, conversationFragment.getClass().getSimpleName());
    }

    public SharedPreferences getDefaultSharedPrefs() {
        return sharedPref;
    }

    @Override
    public void onSuccessSendingPacket(QBSignalingSpec.QBSignalCMD qbSignalCMD, Integer integer) {
    }

    @Override
    public void onErrorSendingPacket(QBSignalingSpec.QBSignalCMD qbSignalCMD, Integer userId, QBRTCSignalException e) {
        showToast(R.string.dlg_signal_error);
    }


    public void onUseHeadSet(boolean use) {
        audioManager.setManageHeadsetByDefault(use);
    }

    public void sendHeadsetState() {
        if (isInCommingCall) {
            onChangeDynamicCallback.enableDynamicToggle(headsetPlugged, previousDeviceEarPiece);
        }
    }

    ////////////////////////////// IncomeCallFragmentCallbackListener ////////////////////////////

    @Override
    public void onAcceptCurrentSession() {
        if (currentSession != null) {
            addConversationFragment(true);
        } else {
            Log.d(TAG, "SKIP addConversationFragment method");
        }
    }

    @Override
    public void onRejectCurrentSession() {
        rejectCurrentSession();
    }
    //////////////////////////////////////////   end   /////////////////////////////////////////////


    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    ////////////////////////////// ConversationFragmentCallbackListener ////////////////////////////

    @Override
    public void addTCClientConnectionCallback(QBRTCSessionStateCallback clientConnectionCallbacks) {
        if (currentSession != null) {
            currentSession.addSessionCallbacksListener(clientConnectionCallbacks);
        }
    }

    @Override
    public void addRTCSessionEventsCallback(QBRTCSessionEventsCallback eventsCallback) {
        QBRTCClient.getInstance(this).addSessionCallbacksListener(eventsCallback);
    }

    @Override
    public void onSetAudioEnabled(boolean isAudioEnabled) {
        setAudioEnabled(isAudioEnabled);
    }

    @Override
    public void onHangUpCurrentSession() {
        if (!isInCommingCall) {
            insertCallHistory(Constant.CALL_STATUS_MISS);
        }
        hangUpCurrentSession();
    }

    @TargetApi(21)
    @Override
    public void onStartScreenSharing() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        QBRTCScreenCapturer.requestPermissions(CallActivity.this);
    }

    @Override
    public void onSwitchCamera(CameraVideoCapturer.CameraSwitchHandler cameraSwitchHandler) {
        ((QBRTCCameraVideoCapturer) (currentSession.getMediaStreamManager().getVideoCapturer()))
                .switchCamera(cameraSwitchHandler);
    }

    @Override
    public void onSetVideoEnabled(boolean isNeedEnableCam) {
        setVideoEnabled(isNeedEnableCam);
    }

    @Override
    public void onSwitchAudio() {
        if (audioManager.getSelectedAudioDevice() == AppRTCAudioManager.AudioDevice.WIRED_HEADSET
                || audioManager.getSelectedAudioDevice() == AppRTCAudioManager.AudioDevice.EARPIECE) {
            audioManager.setAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
        } else {
            audioManager.setAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
        }
    }

    @Override
    public void removeRTCClientConnectionCallback(QBRTCSessionStateCallback clientConnectionCallbacks) {
        if (currentSession != null) {
            currentSession.removeSessionCallbacksListener(clientConnectionCallbacks);
        }
    }

    @Override
    public void removeRTCSessionEventsCallback(QBRTCSessionEventsCallback eventsCallback) {
        QBRTCClient.getInstance(this).removeSessionsCallbacksListener(eventsCallback);
    }

    @Override
    public void addCurrentCallStateCallback(CurrentCallStateCallback currentCallStateCallback) {
        currentCallStateCallbackList.add(currentCallStateCallback);
    }

    @Override
    public void removeCurrentCallStateCallback(CurrentCallStateCallback currentCallStateCallback) {
        currentCallStateCallbackList.remove(currentCallStateCallback);
    }

    @Override
    public void addOnChangeDynamicToggle(OnChangeDynamicToggle onChangeDynamicCallback) {
        this.onChangeDynamicCallback = onChangeDynamicCallback;
        sendHeadsetState();
    }

    @Override
    public void removeOnChangeDynamicToggle(OnChangeDynamicToggle onChangeDynamicCallback) {
        this.onChangeDynamicCallback = null;
    }

    private void notifyCallStateListenersCallStarted() {
        for (CurrentCallStateCallback callback : currentCallStateCallbackList) {
            callback.onCallStarted();
        }
    }

    private void notifyCallStateListenersCallStopped() {
        for (CurrentCallStateCallback callback : currentCallStateCallbackList) {
            callback.onCallStopped();
        }
    }

    private void notifyCallStateListenersNeedUpdateOpponentsList(final ArrayList<QBUser> newUsers) {
        for (CurrentCallStateCallback callback : currentCallStateCallbackList) {
            callback.onOpponentsListUpdated(newUsers);
        }
    }

    public interface OnChangeDynamicToggle {
        void enableDynamicToggle(boolean plugged, boolean wasEarpiece);
    }

    public interface CurrentCallStateCallback {
        void onCallStarted();

        void onCallStopped();

        void onOpponentsListUpdated(ArrayList<QBUser> newUsers);
    }

    //////////////////////////////////////////   end   /////////////////////////////////////////////
    private class ConnectionListener extends AbstractConnectionListener {
        @Override
        public void connectionClosedOnError(Exception e) {
            showNotificationPopUp(R.string.connection_was_lost, true);
            setExpirationReconnectionTime();
        }

        @Override
        public void reconnectionSuccessful() {
            showNotificationPopUp(R.string.connection_was_lost, false);
        }

        @Override
        public void reconnectingIn(int seconds) {
            Log.i(TAG, "reconnectingIn " + seconds);
            if (!callStarted) {
                hangUpAfterLongReconnection();
            }
        }
    }
}
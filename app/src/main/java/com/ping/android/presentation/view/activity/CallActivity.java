package com.ping.android.presentation.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.bzzzchat.cleanarchitecture.scopes.HasComponent;
import com.ping.android.R;
import com.ping.android.dagger.loggedin.call.CallComponent;
import com.ping.android.dagger.loggedin.call.CallModule;
import com.ping.android.data.db.QbUsersDbManager;
import com.ping.android.model.User;
import com.ping.android.model.enums.NetworkStatus;
import com.ping.android.presentation.presenters.CallPresenter;
import com.ping.android.presentation.view.fragment.AudioConversationFragment;
import com.ping.android.presentation.view.fragment.BaseConversationFragment;
import com.ping.android.presentation.view.fragment.BaseFragment;
import com.ping.android.presentation.view.fragment.IncomeCallFragment;
import com.ping.android.presentation.view.fragment.VideoConversationFragment;
import com.ping.android.utils.CommonMethod;
import com.ping.android.utils.FragmentExecuotr;
import com.ping.android.utils.Navigator;
import com.ping.android.utils.NetworkConnectionChecker;
import com.ping.android.utils.PermissionsChecker;
import com.ping.android.utils.RingtonePlayer;
import com.ping.android.utils.SettingsUtil;
import com.ping.android.utils.Toaster;
import com.ping.android.utils.UsersUtils;
import com.ping.android.utils.configs.Consts;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.AppRTCAudioManager;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCScreenCapturer;
import com.quickblox.videochat.webrtc.QBRTCSession;

import org.jivesoftware.smack.AbstractConnectionListener;
import org.webrtc.CameraVideoCapturer;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * QuickBlox team
 */
public class CallActivity extends CoreActivity implements CallPresenter.View,
        NetworkConnectionChecker.OnConnectivityChangedListener, HasComponent<CallComponent> {
    private static final int ACTIVITY_REQUEST_CODE = 100;
    public static final String INCOME_CALL_FRAGMENT = "income_call_fragment";
    private static final String TAG = CallActivity.class.getSimpleName();
    public static final String EXTRA_SESSION_ID = "EXTRA_SESSION_ID";
    private static final String EXTRA_IS_INCOMING_CALL = "EXTRA_IS_INCOMING_CALL";
    private static final String EXTRA_IS_VIDEO_CALL = "EXTRA_IS_VIDEO_CALL";
    public static final String EXTRA_OPPONENT_USER = "EXTRA_OPPONENT_USER";

    private boolean isInComingCall;
    private QBRTCSession currentSession;
    private QBRTCClient rtcClient;
    private ConnectionListener connectionListener;
    private SharedPreferences sharedPref;
    private AppRTCAudioManager audioManager;
    private NetworkConnectionChecker networkConnectionChecker;
    private QbUsersDbManager dbManager;
    private ArrayList<CurrentCallStateCallback> currentCallStateCallbackList = new ArrayList<>();
    private boolean callStarted;
    private boolean isVideoCall;
    private long expirationReconnectionTime;
    private int reconnectHangUpTimeMillis;
    private boolean showToastAfterHeadsetPlugged = true;
    private PermissionsChecker checker;

    private Navigator navigator;
    private RingtonePlayer ringtonePlayer;

    @Inject
    CallPresenter presenter;
    CallComponent component;

    public static void start(Context context, User currentUser, User otherUser, Boolean isVideoCall) {
        int userQBID = otherUser.quickBloxID;

        if (((CoreActivity) context).networkStatus != NetworkStatus.CONNECTED) {
            Toast.makeText(context, "Please check network connection", Toast.LENGTH_SHORT).show();
            return;
        }

        if (CommonMethod.getBooleanFrom(otherUser.blocks, currentUser.key)) {
            String username = otherUser.getFirstName();
            Toaster.shortToast(String.format(context.getString(R.string.msg_account_blocked_by), username));
            return;
        }

        if (CommonMethod.getBooleanFrom(currentUser.blocks, otherUser.key)) {
            String username = otherUser.getFirstName();
            Toaster.shortToast(String.format(context.getString(R.string.msg_account_call_blocked), username, username));
            return;
        }

        if (currentUser.quickBloxID <= 0) {
            Toaster.shortToast(context.getString(R.string.msg_current_user_empty_quickbloxID));
            return;
        }

        if (userQBID <= 0) {
            Toaster.shortToast(context.getString(R.string.msg_opponent_user_empty_quickbloxID));
            return;
        }
        //QBUser opponentQBUser = ServiceManager.getInstance().getQBUserByPingID(userID);
        ArrayList<Integer> opponentsList = new ArrayList<>();
        opponentsList.add(userQBID);
        if (opponentsList.size() == 0) {
            return;
        }
        CallActivity.start(context, otherUser, isVideoCall, false);
    }

    private static void start(Context context, User otherUser, boolean isVideoCall,
                              boolean isIncomingCall) {
        ((CoreActivity) context).startCallService(context);
        Intent intent = new Intent(context, CallActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(EXTRA_IS_VIDEO_CALL, isVideoCall);
        intent.putExtra(EXTRA_IS_INCOMING_CALL, isIncomingCall);
        intent.putExtra(EXTRA_OPPONENT_USER, otherUser);
        context.startActivity(intent);
    }

    public static void start(Context context, String sessionId,
                             boolean isIncomingCall, boolean isVideoCall) {

        Intent intent = new Intent(context, CallActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        intent.putExtra(EXTRA_IS_INCOMING_CALL, isIncomingCall);
        intent.putExtra(EXTRA_SESSION_ID, sessionId);
        intent.putExtra(EXTRA_IS_VIDEO_CALL, isVideoCall);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        getComponent().inject(this);
        navigator = new Navigator();
        navigator.init(getSupportFragmentManager(), R.id.fragment_container);

        initFields();
        isInComingCall = getIntent().getBooleanExtra(EXTRA_IS_INCOMING_CALL, false);
        isVideoCall = getIntent().getBooleanExtra(EXTRA_IS_VIDEO_CALL, false);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        checker = new PermissionsChecker(getApplicationContext());

        initWiFiManagerListener();
        initAudioSettings();
        initQBRTCClient();
        if (checkPermission()) {
            initialized();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra("ENDCALL")) {
            hangUpCurrentSession();
        }
    }

    @Override
    public CallPresenter getPresenter() {
        return presenter;
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
        if (requestCode == ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                initialized();
            } else {
                finish();
            }
        }
    }

    private void initialized() {
        presenter.init(getIntent(), isInComingCall, isVideoCall);
    }

    private boolean checkPermission() {
        if (checker.lacksPermissions(Consts.PERMISSIONS)) {
            startPermissionsActivity(!isVideoCall);
            return false;
        }
        return true;
    }

    private void startPermissionsActivity(boolean checkOnlyAudio) {
        PermissionsActivity.startActivity(this, checkOnlyAudio, ACTIVITY_REQUEST_CODE, Consts.PERMISSIONS);
    }

    private void startLoadAbsentUsers(Integer callerId, List<Integer> opponentsIdsList) {
        ArrayList<QBUser> usersFromDb = dbManager.getAllUsers();
        ArrayList<Integer> allParticipantsOfCall = new ArrayList<>();
        allParticipantsOfCall.addAll(opponentsIdsList);

        if (isInComingCall) {
            allParticipantsOfCall.add(callerId);
        }

        ArrayList<Integer> idsUsersNeedLoad = UsersUtils.getIdsNotLoadedUsers(usersFromDb, allParticipantsOfCall);
        if (!idsUsersNeedLoad.isEmpty()) {
            QBUsers.getUsersByIDs(idsUsersNeedLoad, null).performAsync(new QBEntityCallbackImpl<ArrayList<QBUser>>() {
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

    private void initFields() {
        dbManager = QbUsersDbManager.getInstance(getApplicationContext());
        //opponentsIdsList = currentSession.getOpponents();
    }

    private void setAudioDeviceDelayed(final AppRTCAudioManager.AudioDevice audioDevice) {
        new Handler().postDelayed(() -> {
            showToastAfterHeadsetPlugged = true;
            audioManager.selectAudioDevice(audioDevice);
        }, 500);
    }

    private void initQBRTCClient() {
        rtcClient = QBRTCClient.getInstance(this);

        rtcClient.setCameraErrorHandler(new CameraVideoCapturer.CameraEventsHandler() {
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

        QBRTCConfig.setDebugEnabled(true);


        // Add activity as callback to RTCClient
        //rtcClient.addSessionCallbacksListener(this);
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

    public void hangUpCurrentSession() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment != null && fragment instanceof BaseConversationFragment) {
            ((BaseConversationFragment)fragment).hangup();
        }
//        ringtonePlayer.stop();
//        if (getCurrentSession() != null) {
//            getCurrentSession().hangUp(new HashMap<String, String>());
//        }
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

    public void releaseCurrentSession() {
        Log.d(TAG, "Release current session");
        if (currentSession != null) {
            this.currentSession = null;
        }
    }

    private void showToast(final int message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toaster.shortToast(message);
            }
        });
    }

    private void addConversationFragment(boolean isVideo, boolean isIncomingCall) {
        BaseConversationFragment conversationFragment = isVideo
                ? VideoConversationFragment.newInstance()
                : AudioConversationFragment.newInstance();
        FragmentExecuotr.addFragment(getSupportFragmentManager(), R.id.fragment_container, conversationFragment, conversationFragment.getClass().getSimpleName());
    }

    @Override
    public void onBackPressed() {
    }

    public void onSwitchAudio(boolean isSpeaker) {
        if (isSpeaker) {
            audioManager.selectAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
        } else {
            audioManager.selectAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
        }
    }

    private void notifyCallStateListenersNeedUpdateOpponentsList(final ArrayList<QBUser> newUsers) {
        for (CurrentCallStateCallback callback : currentCallStateCallbackList) {
            callback.onOpponentsListUpdated(newUsers);
        }
    }

    @Override
    public void startInComingCall(boolean isVideoCall) {
        navigator.openAsRoot(new IncomeCallFragment());
        ringtonePlayer.play(isInComingCall, true);
    }

    @Override
    public void startOutgoingCall(User opponentUser, boolean isVideoCall) {
        BaseFragment fragment = isVideoCall ?
                VideoConversationFragment.newInstance() : AudioConversationFragment.newInstance();
        navigator.openAsRoot(fragment);
        ringtonePlayer.play(isInComingCall, true);
    }

    @Override
    public void configCallSettings(List<Integer> users) {
        SettingsUtil.setSettingsStrategy(users, sharedPref, this);
        SettingsUtil.configRTCTimers(this);
    }

    @Override
    public void updateAudioSetting(boolean isIncomingCall, boolean isVideo) {
        if (isVideo) {
            setAudioDeviceDelayed(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
        } else if (isIncomingCall) {
            setAudioDeviceDelayed(AppRTCAudioManager.AudioDevice.EARPIECE);
        }
    }

    public void initAudioSettings() {
        audioManager = AppRTCAudioManager.create(this);
        if (isInComingCall) {
            ringtonePlayer = new RingtonePlayer(this);
            audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
        } else {
            ringtonePlayer = new RingtonePlayer(this, R.raw.beep);
            if (isVideoCall) {
                audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
            } else {
                audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
            }
        }
        audioManager.setOnWiredHeadsetStateListener((plugged, hasMicrophone) -> {
            if (callStarted) {
                //Toaster.shortToast("Headset " + (plugged ? "plugged" : "unplugged"));
            }
        });
        audioManager.start((audioDevice, set) -> {
            if (callStarted) {
                if (showToastAfterHeadsetPlugged) {
                    //Toaster.shortToast("Audio device switched to  " + audioDevice);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRingtone();
    }

    @Override
    public void finishCall() {
        if (audioManager != null) {
            audioManager.stop();
        }
        releaseCurrentSession();
        finish();
    }

    @Override
    public void initCallViews(boolean isVideo, boolean isIncoming) {
        addConversationFragment(isVideo, isIncoming);
        //showOngoingCallNotification();
    }

    @Override
    public void showErrorSendingPacket() {
        showToast(R.string.dlg_signal_error);
    }

    @Override
    public void updateOpponentInfo(User user) {

    }

    @Override
    public void initUserData(Integer callerId, List<Integer> opponents) {
        startLoadAbsentUsers(callerId, opponents);
    }

    @Override
    public void onCallStarted() {
        callStarted = true;
        stopRingtone();
    }

    @Override
    public void stopRingtone() {
        if (ringtonePlayer != null) {
            ringtonePlayer.stop();
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

    @Override
    public CallComponent getComponent() {
        if (component == null) {
            component = getLoggedInComponent().provideCallComponent(new CallModule(this));
        }
        return component;
    }
}
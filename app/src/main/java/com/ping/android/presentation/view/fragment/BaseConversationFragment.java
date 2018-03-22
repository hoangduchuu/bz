package com.ping.android.presentation.view.fragment;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.ping.android.presentation.view.activity.CallActivity;
import com.ping.android.activity.R;
import com.ping.android.data.db.QbUsersDbManager;
import com.ping.android.ultility.Consts;
import com.quickblox.chat.QBChatService;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

public abstract class BaseConversationFragment extends BaseToolBarFragment implements CallActivity.CurrentCallStateCallback {

    private static final String TAG = BaseConversationFragment.class.getSimpleName();
    protected QbUsersDbManager dbManager;
    //protected ArrayList<QBUser> opponents;
    protected Chronometer timerChronometer;
    protected boolean isStarted;
    protected TextView allOpponentsTextView;
    protected TextView ringingTextView;
    protected QBUser currentUser;
    private boolean isIncomingCall;
    private ToggleButton btnToggleMic;
    private ImageButton btnHangup;

    public static BaseConversationFragment newInstance(BaseConversationFragment baseConversationFragment, boolean isIncomingCall) {
        Log.d(TAG, "isIncomingCall =  " + isIncomingCall);
        Bundle args = new Bundle();
        args.putBoolean(Consts.EXTRA_IS_INCOMING_CALL, isIncomingCall);

        baseConversationFragment.setArguments(args);

        return baseConversationFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        initFields();
        initViews(view);
        initButtonsListener();
        prepareAndShowOutgoingScreen();

        return view;
    }

    private void prepareAndShowOutgoingScreen() {
        configureOutgoingScreen();
    }

    protected abstract void configureOutgoingScreen();

    protected void initFields() {
        currentUser = QBChatService.getInstance().getUser();
        dbManager = QbUsersDbManager.getInstance(getActivity().getApplicationContext());

        if (getArguments() != null) {
            isIncomingCall = getArguments().getBoolean(Consts.EXTRA_IS_INCOMING_CALL);
        }

        //initOpponentsList();
    }

    @Override
    public void onStart() {
        super.onStart();
//        if (currentSession == null) {
//            Log.d(TAG, "currentSession = null onStart");
//            return;
//        }
//
//        User currentUser = UserManager.getInstance().getUser();
//        Map<String, String> userInfo = new HashMap();
//        userInfo.put("ping_id", currentUser.pingID);
//        userInfo.put("user_id", currentUser.key);
//        userInfo.put("first_last_name", currentUser.getDisplayName());

//        if (currentSession.getState() != QBRTCSession.QBRTCSessionState.QB_RTC_SESSION_ACTIVE) {
//            if (isIncomingCall) {
//                currentSession.acceptCall(userInfo);
//            } else {
//                //currentSession.startCall(userInfo);
//                //String callType = currentSession.getConferenceType() == QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO? "video": "voice";
//                //NotificationHelper.getInstance().sendCallingNotificationToUser(currentSession.getOpponents().get(0), callType);
//            }
//            isMessageProcessed = true;
//        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    protected abstract void hangup();

    protected void initViews(View view) {
        btnToggleMic = view.findViewById(R.id.toggle_mic);
        btnHangup = view.findViewById(R.id.button_hangup_call);
        allOpponentsTextView = view.findViewById(R.id.text_outgoing_opponents_names);
        ringingTextView = view.findViewById(R.id.text_ringing);

        timerChronometer = view.findViewById(R.id.chronometer_timer_call);

        if (isIncomingCall) {
            hideOutgoingScreen();
        }
    }

    protected void initButtonsListener() {
        btnToggleMic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleAudio(isChecked);
            }
        });

        btnHangup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionButtonsEnabled(false);
                btnHangup.setEnabled(false);
                btnHangup.setActivated(false);
                hangup();
            }
        });
    }

    protected abstract void toggleAudio(boolean isEnable);

    protected void actionButtonsEnabled(boolean inability) {
        btnToggleMic.setEnabled(inability);
        // inactivate toggle buttons
        btnToggleMic.setActivated(inability);
    }

    private void startTimer() {
        if (!isStarted) {
            timerChronometer.setVisibility(View.VISIBLE);
            timerChronometer.setBase(SystemClock.elapsedRealtime());
            timerChronometer.start();
            isStarted = true;
        }
    }

    private void stopTimer() {
        if (timerChronometer != null) {
            timerChronometer.stop();
            isStarted = false;
        }
    }

    protected void hideOutgoingScreen() {
        ringingTextView.setVisibility(View.GONE);
        //outgoingOpponentsRelativeLayout.setVisibility(View.GONE);
    }

    @Override
    public void onCallStarted() {
        hideOutgoingScreen();
        startTimer();
        actionButtonsEnabled(true);
    }

    @Override
    public void onCallStopped() {
        stopTimer();
        actionButtonsEnabled(false);
    }

    @Override
    public void onOpponentsListUpdated(ArrayList<QBUser> newUsers) {
        //initOpponentsList();
    }

//    private void initOpponentsList() {
//        Log.v("UPDATE_USERS", "super initOpponentsList()");
//        ArrayList<QBUser> usersFromDb = dbManager.getUsersByIds(currentSession.getOpponents());
//        opponents = UsersUtils.getListAllUsersFromIds(usersFromDb, currentSession.getOpponents());
//
//        QBUser caller = dbManager.getUserById(currentSession.getCallerID());
//        if (caller == null) {
//            caller = new QBUser(currentSession.getCallerID());
//            caller.setFullName(String.valueOf(currentSession.getCallerID()));
//        }
//
//        if (isIncomingCall) {
//            opponents.add(caller);
//            opponents.remove(QBChatService.getInstance().getUser());
//        }
//    }

}
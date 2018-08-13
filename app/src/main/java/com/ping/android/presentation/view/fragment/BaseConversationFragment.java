package com.ping.android.presentation.view.fragment;

import android.os.Bundle;
import android.os.SystemClock;
import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.ping.android.R;
import com.ping.android.data.db.QbUsersDbManager;
import com.ping.android.presentation.view.activity.CallActivity;
import com.ping.android.utils.configs.Consts;
import com.quickblox.chat.QBChatService;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

public abstract class BaseConversationFragment extends BaseToolBarFragment implements CallActivity.CurrentCallStateCallback {
    private static final String TAG = BaseConversationFragment.class.getSimpleName();

    protected QbUsersDbManager dbManager;
    protected QBUser currentUser;
    protected Chronometer timerChronometer;
    protected TextView allOpponentsTextView;
    protected TextView ringingTextView;

    protected boolean isStarted;
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    protected abstract void hangup(double duration);

    @CallSuper
    public void hangup() {
        double diff = (SystemClock.elapsedRealtime() - timerChronometer.getBase()) / 1000;
        hangup(diff);
    }

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
        btnToggleMic.setOnCheckedChangeListener((buttonView, isChecked) -> toggleAudio(isChecked));

        btnHangup.setOnClickListener(v -> {
            actionButtonsEnabled(false);
            btnHangup.setEnabled(false);
            btnHangup.setActivated(false);
            hangup();
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
        hangup();
        actionButtonsEnabled(false);
    }

    @Override
    public void onOpponentsListUpdated(ArrayList<QBUser> newUsers) {
        //initOpponentsList();
    }
}
package com.ping.android.fragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ping.android.activity.R;
import com.ping.android.dagger.loggedin.call.CallComponent;
import com.ping.android.dagger.loggedin.call.incoming.IncomingCallComponent;
import com.ping.android.dagger.loggedin.call.incoming.IncomingCallModule;
import com.ping.android.presentation.presenters.IncomingCallPresenter;
import com.ping.android.utils.RingtonePlayer;
import com.ping.android.utils.UiUtils;
import com.quickblox.videochat.webrtc.QBRTCTypes;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

/**
 * QuickBlox team
 */
public class IncomeCallFragment extends BaseFragment implements Serializable, View.OnClickListener, IncomingCallPresenter.View {

    private static final String TAG = IncomeCallFragment.class.getSimpleName();
    private static final long CLICK_DELAY = TimeUnit.SECONDS.toMillis(2);
    private TextView callTypeTextView;
    private ImageButton rejectButton;
    private ImageButton takeButton;
    private TextView callerNameTextView;
    private ImageView callerAvatarImageView;

    private Vibrator vibrator;
    private long lastClickTime = 0l;
    private RingtonePlayer ringtonePlayer;
//    private IncomeCallFragmentCallbackListener incomeCallFragmentCallbackListener;

    @Inject
    IncomingCallPresenter presenter;
    IncomingCallComponent component;

//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);

//        try {
//            incomeCallFragmentCallbackListener = (IncomeCallFragmentCallbackListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnCallEventsController");
//        }
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
        getComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_income_call, container, false);
        initUI(view);
        initButtonsListener();
        ringtonePlayer = new RingtonePlayer(getActivity());
        presenter.create();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        startCallNotification();
    }

    private void initButtonsListener() {
        rejectButton.setOnClickListener(this);
        takeButton.setOnClickListener(this);
    }

    private void initUI(View view) {
        callTypeTextView = view.findViewById(R.id.call_type);
        callerAvatarImageView = view.findViewById(R.id.image_caller_avatar);
        callerNameTextView = view.findViewById(R.id.text_caller_name);
        rejectButton = view.findViewById(R.id.image_button_reject_call);
        takeButton = view.findViewById(R.id.image_button_accept_call);
    }

    private Drawable getBackgroundForCallerAvatar(int callerId) {
        return UiUtils.getColorCircleDrawable(callerId);
    }

    public void startCallNotification() {
        ringtonePlayer.play(true);
        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        long[] vibrationCycle = {0, 1000, 1000};
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(vibrationCycle, 1);
        }

    }

    private void stopCallNotification() {
        Log.d(TAG, "stopCallNotification()");

        if (ringtonePlayer != null) {
            ringtonePlayer.stop();
        }

        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    @Override
    public void onStop() {
        stopCallNotification();
        super.onStop();
        Log.d(TAG, "onStop() from IncomeCallFragment");
    }

    @Override
    public void onClick(View v) {

        if ((SystemClock.uptimeMillis() - lastClickTime) < CLICK_DELAY) {
            return;
        }
        lastClickTime = SystemClock.uptimeMillis();

        switch (v.getId()) {
            case R.id.image_button_reject_call:
                reject();
                break;

            case R.id.image_button_accept_call:
                accept();
                break;

            default:
                break;
        }
    }

    private void accept() {
        enableButtons(false);
        stopCallNotification();
        presenter.accept();
        //incomeCallFragmentCallbackListener.onAcceptCurrentSession();
        Log.d(TAG, "Call is started");
    }

    private void reject() {
        enableButtons(false);
        stopCallNotification();
        presenter.reject();
        //incomeCallFragmentCallbackListener.onRejectCurrentSession();
        Log.d(TAG, "Call is rejected");
    }

    private void enableButtons(boolean enable) {
        takeButton.setEnabled(enable);
        rejectButton.setEnabled(enable);
    }

    public IncomingCallComponent getComponent() {
        if (component == null) {
            component = getComponent(CallComponent.class)
                    .provideIncomingCallComponent(new IncomingCallModule(this));
        }
        return component;
    }

    @Override
    public void showConferenceType(QBRTCTypes.QBConferenceType conferenceType) {
        boolean isVideoCall = conferenceType == QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO;
        callTypeTextView.setText(isVideoCall ? R.string.text_incoming_video_call : R.string.text_incoming_audio_call);
    }

    @Override
    public void showOpponentInfo(String displayName, String avatar) {
        callerNameTextView.setText(displayName);
        UiUtils.displayProfileAvatar(callerAvatarImageView, avatar);
        if (!TextUtils.isEmpty(displayName)) {
            callerAvatarImageView.setBackgroundDrawable(getBackgroundForCallerAvatar(displayName.length()));
        }
    }
}

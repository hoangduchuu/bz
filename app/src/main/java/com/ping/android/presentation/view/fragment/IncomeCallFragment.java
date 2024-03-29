package com.ping.android.presentation.view.fragment;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ping.android.R;
import com.ping.android.presentation.presenters.IncomingCallPresenter;
import com.ping.android.utils.UiUtils;
import com.quickblox.videochat.webrtc.QBRTCTypes;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

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
    private long lastClickTime = 0L;

    @Inject
    IncomingCallPresenter presenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
        AndroidSupportInjection.inject(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_income_call, container, false);
        initUI(view);
        initButtonsListener();
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

    public void startCallNotification() {
        if (getActivity() != null) {
            AudioManager audioManager = (AudioManager)getActivity().getSystemService(Context.AUDIO_SERVICE);
            if (audioManager == null
                    || audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
                return;
            }
            vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
            long[] vibrationCycle = {0, 1000, 1000};
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(vibrationCycle, 1);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() from IncomeCallFragment");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (vibrator != null) {
            vibrator.cancel();
        }
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
        disableButtons();
        stopCallNotification();
        presenter.accept();
    }

    private void reject() {
        disableButtons();
        stopCallNotification();
        presenter.reject();
    }

    private void disableButtons() {
        takeButton.setEnabled(false);
        rejectButton.setEnabled(false);
    }

    @Override
    public void showConferenceType(QBRTCTypes.QBConferenceType conferenceType) {
        boolean isVideoCall = conferenceType == QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO;
        callTypeTextView.setText(isVideoCall ? R.string.text_incoming_video_call : R.string.text_incoming_audio_call);
    }

    @Override
    public void showOpponentInfo(String displayName, String avatar) {
        callerNameTextView.setText(displayName);
        UiUtils.displayProfileAvatar(callerAvatarImageView, avatar, R.drawable.ic_avatar_orange);
    }

    @Override
    public void stopCallNotification() {
        if (vibrator != null) {
            vibrator.cancel();
        }
    }
}

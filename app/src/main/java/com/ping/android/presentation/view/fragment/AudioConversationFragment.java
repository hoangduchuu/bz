package com.ping.android.presentation.view.fragment;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.ping.android.R;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.AudioCallPresenter;
import com.ping.android.presentation.view.activity.CallActivity;
import com.ping.android.utils.UiUtils;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

public class AudioConversationFragment extends BaseConversationFragment
        implements CallActivity.OnChangeDynamicToggle, AudioCallPresenter.View {
    private static final String TAG = AudioConversationFragment.class.getSimpleName();

    private ToggleButton audioSwitchToggleButton;
    private ImageView opponentImage;

    private boolean headsetPlugged;

    @Inject
    AudioCallPresenter presenter;

    public static AudioConversationFragment newInstance() {
        AudioConversationFragment fragment = new AudioConversationFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidSupportInjection.inject(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        //conversationFragmentCallbackListener.addOnChangeDynamicToggle(this);
    }

    @Override
    protected void hangup(double duration) {
        presenter.hangup(duration);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        presenter.create();
        return view;
    }

    @Override
    protected void configureOutgoingScreen() {
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        timerChronometer = view.findViewById(R.id.chronometer_timer_call);
        opponentImage = view.findViewById(R.id.image_caller_avatar);
        audioSwitchToggleButton = view.findViewById(R.id.toggle_speaker);
        audioSwitchToggleButton.setVisibility(View.VISIBLE);

        actionButtonsEnabled(true);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void initButtonsListener() {
        super.initButtonsListener();

        audioSwitchToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //conversationFragmentCallbackListener.onSwitchAudio();
                Activity activity = getActivity();
                if (activity instanceof CallActivity) {
                    ((CallActivity) activity).onSwitchAudio(audioSwitchToggleButton.isChecked());
                }
            }
        });
    }

    @Override
    protected void toggleAudio(boolean isEnable) {
        presenter.toggleAudio(isEnable);
    }

    @Override
    protected void actionButtonsEnabled(boolean inability) {
        super.actionButtonsEnabled(inability);
        if (!headsetPlugged) {
            audioSwitchToggleButton.setEnabled(inability);
        }
        audioSwitchToggleButton.setActivated(false);
    }

    @Override
    int getFragmentLayout() {
        return R.layout.fragment_audio_conversation;
    }

    @Override
    public void onOpponentsListUpdated(ArrayList<QBUser> newUsers) {
        super.onOpponentsListUpdated(newUsers);
    }

    @Override
    public void initAudio(boolean isVideo) {

    }

    @Override
    public void enableDynamicToggle(boolean plugged, boolean previousDeviceEarPiece) {
        headsetPlugged = plugged;

        audioSwitchToggleButton.setEnabled(!plugged);
        if (plugged) {
            audioSwitchToggleButton.setChecked(false);
        } else if (previousDeviceEarPiece) {
            audioSwitchToggleButton.setChecked(false);
        } else {
            audioSwitchToggleButton.setChecked(true);
        }

    }

    @Override
    public void updateOpponentInfo(User opponentInfo) {
        UiUtils.displayProfileAvatar(opponentImage, opponentInfo.profile, R.drawable.ic_avatar_orange);
        allOpponentsTextView.setText(opponentInfo.nickName);
    }
}

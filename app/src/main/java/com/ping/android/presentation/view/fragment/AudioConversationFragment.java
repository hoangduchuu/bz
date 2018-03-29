package com.ping.android.presentation.view.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.ping.android.presentation.view.activity.CallActivity;
import com.ping.android.activity.R;
import com.ping.android.dagger.loggedin.call.CallComponent;
import com.ping.android.dagger.loggedin.call.audio.AudioCallComponent;
import com.ping.android.dagger.loggedin.call.audio.AudioCallModule;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.AudioCallPresenter;
import com.ping.android.utils.UiUtils;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

import javax.inject.Inject;

public class AudioConversationFragment extends BaseConversationFragment
        implements CallActivity.OnChangeDynamicToggle, AudioCallPresenter.View {
    private static final String TAG = AudioConversationFragment.class.getSimpleName();

    private ToggleButton audioSwitchToggleButton;
    private ImageView opponentImage;

    private boolean headsetPlugged;

    @Inject
    AudioCallPresenter presenter;
    AudioCallComponent component;

    public static AudioConversationFragment newInstance() {
        AudioConversationFragment fragment = new AudioConversationFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getComponent().inject(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        //conversationFragmentCallbackListener.addOnChangeDynamicToggle(this);
    }

    @Override
    protected void hangup() {
        presenter.hangup();
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
        allOpponentsTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_color_outgoing_opponents_names_audio_call));
        ringingTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_color_call_type));
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        timerChronometer = view.findViewById(R.id.chronometer_timer_call);
        opponentImage = view.findViewById(R.id.image_caller_avatar);
        audioSwitchToggleButton = view.findViewById(R.id.toggle_speaker);
        audioSwitchToggleButton.setVisibility(View.VISIBLE);

        actionButtonsEnabled(false);
    }

    @Override
    public void onStop() {
        super.onStop();
        //conversationFragmentCallbackListener.removeOnChangeDynamicToggle(this);
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
                    ((CallActivity) activity).onSwitchAudio();
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
        audioSwitchToggleButton.setChecked(false);
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

    public AudioCallComponent getComponent() {
        if (component == null) {
            component = getComponent(CallComponent.class).provideAudioCallComponent(new AudioCallModule(this));
        }
        return component;
    }

    @Override
    public void updateOpponentInfo(User opponentInfo) {
        UiUtils.displayProfileImage(getContext(), opponentImage, opponentInfo);
        allOpponentsTextView.setText(opponentInfo.nickName);
    }
}

package com.ping.android.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.ping.android.activity.CallActivity;
import com.ping.android.activity.R;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.utils.UiUtils;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

public class AudioConversationFragment extends BaseConversationFragment implements CallActivity.OnChangeDynamicToggle {
    private static final String TAG = AudioConversationFragment.class.getSimpleName();

    private ToggleButton audioSwitchToggleButton;
    private boolean headsetPlugged;
    private User opponentUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        conversationFragmentCallbackListener.addOnChangeDynamicToggle(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void configureOutgoingScreen() {
        allOpponentsTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_color_outgoing_opponents_names_audio_call));
        ringingTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_color_call_type));
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        timerChronometer = (Chronometer) view.findViewById(R.id.chronometer_timer_call);

        ImageView firstOpponentAvatarImageView = (ImageView) view.findViewById(R.id.image_caller_avatar);
        opponentUser = ServiceManager.getInstance().getUserByQBId(opponents.get(0).getId());
        UiUtils.displayProfileImage(getContext(), firstOpponentAvatarImageView, opponentUser);

        allOpponentsTextView.setText(opponentUser.getDisplayName());

        audioSwitchToggleButton = (ToggleButton) view.findViewById(R.id.toggle_speaker);
        audioSwitchToggleButton.setVisibility(View.VISIBLE);

        actionButtonsEnabled(false);
    }

    @Override
    public void onStop() {
        super.onStop();
        conversationFragmentCallbackListener.removeOnChangeDynamicToggle(this);
    }

    @Override
    protected void initButtonsListener() {
        super.initButtonsListener();

        audioSwitchToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                conversationFragmentCallbackListener.onSwitchAudio();
            }
        });
    }

    @Override
    protected void actionButtonsEnabled(boolean inability) {
        super.actionButtonsEnabled(inability);
        if (!headsetPlugged) {
            audioSwitchToggleButton.setEnabled(inability);
        }
        audioSwitchToggleButton.setActivated(inability);
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
    public void enableDynamicToggle(boolean plugged, boolean previousDeviceEarPiece) {
        headsetPlugged = plugged;

        if (isStarted) {
            audioSwitchToggleButton.setEnabled(!plugged);

            if (plugged) {
                audioSwitchToggleButton.setChecked(true);
            } else if (previousDeviceEarPiece) {
                audioSwitchToggleButton.setChecked(true);
            } else {
                audioSwitchToggleButton.setChecked(false);
            }
        }
    }
}

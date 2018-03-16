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
import com.ping.android.service.firebase.UserRepository;
import com.ping.android.ultility.Callback;
import com.ping.android.utils.UiUtils;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

public class AudioConversationFragment extends BaseConversationFragment implements CallActivity.OnChangeDynamicToggle {
    private static final String TAG = AudioConversationFragment.class.getSimpleName();

    private ToggleButton audioSwitchToggleButton;
    private boolean headsetPlugged;
    private User opponentUser;
    private UserRepository userRepository;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userRepository = new UserRepository();
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
        timerChronometer = view.findViewById(R.id.chronometer_timer_call);

        ImageView firstOpponentAvatarImageView = view.findViewById(R.id.image_caller_avatar);
        userRepository.getUserByQbId(opponents.get(0).getId(), (error, data) -> {
            if (error == null) {
                opponentUser = (User) data[0];
                UiUtils.displayProfileImage(getContext(), firstOpponentAvatarImageView, opponentUser);

                allOpponentsTextView.setText(opponentUser.getDisplayName());
            }
        });

        audioSwitchToggleButton = view.findViewById(R.id.toggle_speaker);
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
    public void enableDynamicToggle(boolean plugged, boolean previousDeviceEarPiece) {
        headsetPlugged = plugged;

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

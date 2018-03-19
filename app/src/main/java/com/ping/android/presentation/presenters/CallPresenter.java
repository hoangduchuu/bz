package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.ping.android.activity.CallActivity;
import com.ping.android.model.User;

import org.webrtc.CameraVideoCapturer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tuanluong on 3/16/18.
 */

public interface CallPresenter extends BasePresenter {
    void initSession(String id, boolean isIncomingCall);

    void initCall(User opponentUser, boolean isVideoCall, boolean isIncomingCall);

    void reject();

    void accept();

    void hangup();

    void toggleAudio(boolean isAudioEnabled);

    void registerCallStateListener(CallActivity.CurrentCallStateCallback callback);

    void removeCallStateCallback(CallActivity.CurrentCallStateCallback callback);

    void switchCamera(CameraVideoCapturer.CameraSwitchHandler cameraSwitchHandler);

    interface View {

        void configCallSettings(List<Integer> users);

        void initAudioSettings(boolean isVideo);

        void finishCall();

        void initCallViews(boolean isVideo, boolean isIncoming);

        void stopRingtone();

        void showErrorSendingPacket();

        void updateOpponentInfo(User user);

        void initUserData(Integer callerId, List<Integer> opponents);

        void sendMissedCallNotification(String userId, int quickBloxID);
    }
}

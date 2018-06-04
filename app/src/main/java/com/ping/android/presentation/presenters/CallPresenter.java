package com.ping.android.presentation.presenters;

import android.content.Intent;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.ping.android.presentation.view.activity.CallActivity;
import com.ping.android.model.User;
import com.quickblox.videochat.webrtc.QBRTCSession;

import org.webrtc.CameraVideoCapturer;

import java.util.List;

/**
 * Created by tuanluong on 3/16/18.
 */

public interface CallPresenter extends BasePresenter {
    void init(Intent intent, boolean isInComingCall, boolean isVideoCall);

    void initSession(String id, boolean isIncomingCall);

    void initCall(User opponentUser, boolean isVideoCall, boolean isIncomingCall);

    void reject();

    void accept();

    void hangup(double duration);

    void toggleAudio(boolean isAudioEnabled);

    void registerCallStateListener(CallActivity.CurrentCallStateCallback callback);

    void removeCallStateCallback(CallActivity.CurrentCallStateCallback callback);

    void switchCamera(CameraVideoCapturer.CameraSwitchHandler cameraSwitchHandler);

    QBRTCSession getCurrentSession();

    User getOpponentUser();

    boolean isIncomingCall();

    interface View {
        void startInComingCall(boolean isVideoCall);

        void startOutgoingCall(User opponentUser, boolean isVideoCall);

        void configCallSettings(List<Integer> users);

        void updateAudioSetting(boolean isIncomingCall, boolean isVideo);

        void finishCall();

        void initCallViews(boolean isVideo, boolean isIncoming);

        void showErrorSendingPacket();

        void updateOpponentInfo(User user);

        void initUserData(Integer callerId, List<Integer> opponents);

        void onCallStarted();

        void stopRingtone();
    }
}

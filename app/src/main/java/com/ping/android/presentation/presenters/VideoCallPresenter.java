package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;
import com.ping.android.model.User;
import com.quickblox.users.model.QBUser;

import org.webrtc.CameraVideoCapturer;

import java.util.ArrayList;

/**
 * Created by tuanluong on 3/20/18.
 */

public interface VideoCallPresenter extends BasePresenter {
    void hangup();

    void toggleAudio(boolean isEnable);

    void switchCamera(CameraVideoCapturer.CameraSwitchHandler cameraSwitchHandler);

    void toggleVideoLocal(boolean isNeedEnableCam);

    interface View extends BaseView {

        void updateOpponentInfo(User opponentUser);

        void playRingtone();

        void onCallStarted();

        void onCallStopped();

        void onOpponentsListUpdated(ArrayList<QBUser> newUsers);
    }
}

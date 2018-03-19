package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.ping.android.activity.CallActivity;

import java.util.List;

/**
 * Created by tuanluong on 3/16/18.
 */

public interface CallPresenter extends BasePresenter {
    void initSession(String id);

    void reject();

    void accept();

    void hangup();

    void toggleAudio(boolean isAudioEnabled);

    void registerCallStateListener(CallActivity.CurrentCallStateCallback callback);

    void removeCallStateCallback(CallActivity.CurrentCallStateCallback callback);

    interface View {

        void configCallSettings(List<Integer> users);

        void initAudioSettings(boolean isVideo);

        void finishCall();

        void initCallViews(boolean isVideo, boolean isIncoming);
    }
}

package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;
import com.quickblox.videochat.webrtc.QBRTCTypes;

/**
 * Created by tuanluong on 3/20/18.
 */

public interface IncomingCallPresenter extends BasePresenter {
    void reject();

    void accept();

    interface View extends BaseView {

        void showConferenceType(QBRTCTypes.QBConferenceType conferenceType);

        void showOpponentInfo(String displayName, String avatar);

        void stopCallNotification();
    }
}

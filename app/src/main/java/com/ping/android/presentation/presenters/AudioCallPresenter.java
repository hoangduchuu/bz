package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;
import com.ping.android.model.User;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

/**
 * Created by tuanluong on 3/20/18.
 */

public interface AudioCallPresenter extends BasePresenter {
    void hangup();

    void toggleAudio(boolean isEnable);

    interface View extends BaseView {
        void updateOpponentInfo(User opponentInfo);

        void onOpponentsListUpdated(ArrayList<QBUser> newUsers);

        void onCallStarted();

        void onCallStopped();
    }
}

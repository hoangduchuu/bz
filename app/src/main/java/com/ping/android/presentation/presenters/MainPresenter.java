package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;

import java.util.Map;

/**
 * Created by tuanluong on 2/10/18.
 */

public interface MainPresenter extends BasePresenter {
    void removeMissedCallsBadge();

    void onNetworkAvailable();

    void turnOffMappingConfirmation();

    void randomizeTransphabet(Map<String, String> maps);

    interface View extends BaseView {

        void openPhoneRequireView();

        void showMappingConfirm();

        void startCallService();
    }
}

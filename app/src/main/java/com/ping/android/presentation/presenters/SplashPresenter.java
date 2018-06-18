package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;
import com.ping.android.model.enums.Color;

/**
 * Created by tuanluong on 2/6/18.
 */

public interface SplashPresenter extends BasePresenter {
    void initializeUser();

    void finishTimer();

    void handleNewConversation(String conversationId);

    interface View extends BaseView {

        void navigateToMainScreen();

        void navigateToLoginScreen();

        void startCallService();

        void showAppUpdateDialog(String appId, String currentVersion);

        void navigateToMainScreenWithExtra(String conversationId);
    }
}

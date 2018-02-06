package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;

/**
 * Created by tuanluong on 2/6/18.
 */

public interface SplashPresenter extends BasePresenter {
    void initializeUser();

    void finishTimer();

    interface View extends BaseView {

        void navigateToMainScreen();

        void navigateToLoginScreen();
    }
}

package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;

/**
 * Created by tuanluong on 2/6/18.
 */

public interface LoginPresenter extends BasePresenter {
    void initializeUser();

    void login(String name, String password);

    interface View extends BaseView {

        void navigateToMainScreen();

        void showMessageLoginFailed();

        void showTimeOutNotification();

    }
}

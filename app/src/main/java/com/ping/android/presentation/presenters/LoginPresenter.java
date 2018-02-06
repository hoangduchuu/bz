package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;

/**
 * Created by tuanluong on 2/6/18.
 */

public interface LoginPresenter extends BasePresenter {
    void initializeUser();

    interface View extends BaseView {

        void navigateToMainScreen();
    }
}

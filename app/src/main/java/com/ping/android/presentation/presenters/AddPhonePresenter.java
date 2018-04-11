package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;

public interface AddPhonePresenter extends BasePresenter {
    void updatePhone(String phoneNumber);

    interface View extends BaseView {

    }
}

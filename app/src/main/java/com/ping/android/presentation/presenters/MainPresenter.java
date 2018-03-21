package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;

/**
 * Created by tuanluong on 2/10/18.
 */

public interface MainPresenter extends BasePresenter {
    interface View extends BaseView {

        void openPhoneRequireView();

        void showMappingConfirm();
    }
}

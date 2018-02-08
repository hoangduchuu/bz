package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;

/**
 * Created by tuanluong on 2/8/18.
 */

public interface ProfilePresenter extends BasePresenter {
    void logout();

    interface View extends BaseView {

    }
}

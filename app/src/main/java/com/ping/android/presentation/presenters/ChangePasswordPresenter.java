package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;
import com.ping.android.model.User;

/**
 * Created by tuanluong on 3/22/18.
 */

public interface ChangePasswordPresenter extends BasePresenter {

    interface View extends BaseView {
        void onUserUpdated(User currentUser);
    }
}

package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;
import com.ping.android.model.User;

/**
 * Created by tuanluong on 3/21/18.
 */

public interface SelectContactPresenter extends BasePresenter {
    interface View extends BaseView {
        void addFriend(User data);
    }
}

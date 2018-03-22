package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;
import com.ping.android.model.User;

/**
 * Created by tuanluong on 3/21/18.
 */

public interface BlockContactPresenter extends BasePresenter {
    void toggleBlockUser(String userId, boolean isBlock);

    interface View extends BaseView {

        void removeBlockedUser(String key);

        void addBlockedUser(User data);
    }
}

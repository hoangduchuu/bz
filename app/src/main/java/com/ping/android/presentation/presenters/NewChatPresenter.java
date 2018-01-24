package com.ping.android.presentation.presenters;

import com.ping.android.model.User;
import com.tl.cleanarchitecture.BasePresenter;
import com.tl.cleanarchitecture.BaseView;

import java.util.List;

/**
 * Created by tuanluong on 1/22/18.
 */

public interface NewChatPresenter extends BasePresenter {
    void searchUsers(String text);

    interface NewChatView extends BaseView {
        void displaySearchResult(List<User> users);
    }
}

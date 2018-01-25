package com.ping.android.presentation.presenters;

import com.ping.android.model.User;
import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;

import java.util.List;

/**
 * Created by tuanluong on 1/25/18.
 */

public interface SearchUserPresenter extends BasePresenter {
    void searchUsers(String text);

    interface View extends BaseView {
        void displaySearchResult(List<User> users);
        void showNoResults();
        void hideNoResults();
        void showSearching();
        void hideSearching();
    }
}

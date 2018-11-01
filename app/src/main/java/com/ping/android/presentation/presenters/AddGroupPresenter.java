package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;
import com.ping.android.model.User;

import java.util.List;

/**
 * Created by tuanluong on 2/8/18.
 */

public interface AddGroupPresenter extends BasePresenter {
    void createGroup(List<User> toUsers, String groupNames, String groupProfileImage, String s);

    void handlePickerPress();

    interface View extends BaseView {
        void moveToChatScreen(String conversationId);

        void initProfileImagePath(String key);

        void openPicker();
    }
}

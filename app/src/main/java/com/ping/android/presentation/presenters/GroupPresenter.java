package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;
import com.ping.android.model.Group;

/**
 * Created by tuanluong on 1/29/18.
 */

public interface GroupPresenter extends BasePresenter {
    void getGroups();

    interface View extends BaseView {
        void addGroup(Group group);
        void updateGroup(Group group);
        void deleteGroup(Group data);
    }
}

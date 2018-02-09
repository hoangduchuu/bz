package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;

/**
 * Created by tuanluong on 2/8/18.
 */

public interface AddGroupPresenter extends BasePresenter {
    void createGroup(String profileImage);

    interface View extends BaseView {
    }
}

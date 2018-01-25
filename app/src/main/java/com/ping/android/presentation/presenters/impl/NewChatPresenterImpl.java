package com.ping.android.presentation.presenters.impl;

import com.ping.android.presentation.presenters.NewChatPresenter;

import javax.inject.Inject;

/**
 * Created by tuanluong on 1/22/18.
 */

public class NewChatPresenterImpl implements NewChatPresenter {
    @Inject
    public NewChatView view;

    @Inject
    public NewChatPresenterImpl() {
    }
}

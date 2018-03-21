package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.ObserveFriendsChildEventUseCase;
import com.ping.android.model.ChildData;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.SelectContactPresenter;

import javax.inject.Inject;

/**
 * Created by tuanluong on 3/21/18.
 */

public class SelectContactPresenterImpl implements SelectContactPresenter {
    @Inject
    View view;
    @Inject
    ObserveFriendsChildEventUseCase observeFriendsChildEventUseCase;

    @Inject
    public SelectContactPresenterImpl() {}

    @Override
    public void create() {
        observeFriendsChildEventUseCase.execute(new DefaultObserver<ChildData<User>>() {
            @Override
            public void onNext(ChildData<User> userChildData) {
                switch (userChildData.type) {
                    case CHILD_ADDED:
                        view.addFriend(userChildData.data);
                        break;
                    default:
                        //view.removeFriend(userChildData.data.key);
                        break;
                }
            }
        }, null);
    }

    @Override
    public void destroy() {
        observeFriendsChildEventUseCase.dispose();
    }
}

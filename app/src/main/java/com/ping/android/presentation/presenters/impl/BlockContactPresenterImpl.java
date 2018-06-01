package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.ObserveBlockedContactsUseCase;
import com.ping.android.domain.usecase.user.ToggleBlockUserUseCase;
import com.ping.android.data.entity.ChildData;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.BlockContactPresenter;

import javax.inject.Inject;

/**
 * Created by tuanluong on 3/21/18.
 */

public class BlockContactPresenterImpl implements BlockContactPresenter {
    @Inject
    View view;
    @Inject
    ObserveBlockedContactsUseCase blockedContactsUseCase;
    @Inject
    ToggleBlockUserUseCase blockUserUseCase;

    @Inject
    public BlockContactPresenterImpl() {}

    @Override
    public void create() {
        blockedContactsUseCase.execute(new DefaultObserver<ChildData<User>>() {
            @Override
            public void onNext(ChildData<User> userChildData) {
                switch (userChildData.getType()) {
                    case CHILD_ADDED:
                        view.addBlockedUser(userChildData.getData());
                        break;
                    case CHILD_REMOVED:
                        view.removeBlockedUser(userChildData.getData().key);
                        break;
                }
            }
        }, null);
    }

    @Override
    public void toggleBlockUser(String userId, boolean isBlock) {
        ToggleBlockUserUseCase.Params params = new ToggleBlockUserUseCase.Params(userId, isBlock);
        blockUserUseCase.execute(new DefaultObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {

            }
        }, params);
    }
}

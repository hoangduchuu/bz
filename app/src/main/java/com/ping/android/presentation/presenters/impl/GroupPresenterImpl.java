package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.ObserveGroupUseCase;
import com.ping.android.model.ChildData;
import com.ping.android.model.Group;
import com.ping.android.presentation.presenters.GroupPresenter;

import javax.inject.Inject;

/**
 * Created by tuanluong on 1/29/18.
 */

public class GroupPresenterImpl implements GroupPresenter {
    @Inject
    ObserveGroupUseCase observeGroupUseCase;
    @Inject
    GroupPresenter.View view;

    @Inject
    public GroupPresenterImpl() {}

    @Override
    public void getGroups() {
        observeGroupUseCase.execute(new DefaultObserver<ChildData<Group>>() {
            @Override
            public void onNext(ChildData<Group> groupChildData) {
                switch (groupChildData.type) {
                    case CHILD_ADDED:
                        view.addGroup(groupChildData.data);
                        break;
                    case CHILD_CHANGED:
                        view.updateGroup(groupChildData.data);
                        break;
                    case CHILD_REMOVED:
                        view.deleteGroup(groupChildData.data);
                }
            }
        }, new ObserveGroupUseCase.Params(true));
    }

    @Override
    public void destroy() {
        observeGroupUseCase.dispose();
    }
}

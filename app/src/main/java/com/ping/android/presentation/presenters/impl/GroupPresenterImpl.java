package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.ObserveGroupsUseCase;
import com.ping.android.domain.usecase.conversation.CreateGroupConversationUseCase;
import com.ping.android.model.ChildData;
import com.ping.android.model.Group;
import com.ping.android.presentation.presenters.GroupPresenter;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * Created by tuanluong on 1/29/18.
 */

public class GroupPresenterImpl implements GroupPresenter {
    @Inject
    ObserveGroupsUseCase observeGroupsUseCase;
    @Inject
    CreateGroupConversationUseCase createGroupConversationUseCase;
    @Inject
    GroupPresenter.View view;

    @Inject
    public GroupPresenterImpl() {}

    @Override
    public void getGroups() {
        observeGroupsUseCase.execute(new DefaultObserver<ChildData<Group>>() {
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

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
            }
        }, new ObserveGroupsUseCase.Params(true));
    }

    @Override
    public void createConversation(Group group) {
        view.showLoading();
        createGroupConversationUseCase.execute(new DefaultObserver<String>() {
            @Override
            public void onNext(String s) {
                view.hideLoading();
                view.moveToChatScreen(s);
            }
        }, group);
    }

    @Override
    public void destroy() {
        observeGroupsUseCase.dispose();
        createGroupConversationUseCase.dispose();
    }
}

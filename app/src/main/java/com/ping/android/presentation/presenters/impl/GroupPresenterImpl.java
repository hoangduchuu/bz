package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.data.entity.ChildData;
import com.ping.android.domain.usecase.ObserveGroupsUseCase;
import com.ping.android.domain.usecase.conversation.CreateGroupConversationUseCase;
import com.ping.android.domain.usecase.conversation.GetConversationValueUseCase;
import com.ping.android.model.Conversation;
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
    GetConversationValueUseCase getConversationValueUseCase;
    @Inject
    GroupPresenter.View view;

    @Inject
    public GroupPresenterImpl() {}

    @Override
    public void getGroups() {
        observeGroupsUseCase.execute(new DefaultObserver<ChildData<Group>>() {
            @Override
            public void onNext(ChildData<Group> groupChildData) {
                switch (groupChildData.getType()) {
                    case CHILD_ADDED:
                        view.addGroup(groupChildData.getData());
                        break;
                    case CHILD_CHANGED:
                        view.updateGroup(groupChildData.getData());
                        break;
                    case CHILD_REMOVED:
                        view.deleteGroup(groupChildData.getData());
                }
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
            }
        }, new ObserveGroupsUseCase.Params(true));
    }

    @Override
    public void handleGroupPress(Group group) {
        if (group.conversationID != null && group.conversationID.length() > 0) {
            getConversationValueUseCase.execute(new DefaultObserver<Conversation>() {
                @Override
                public void onNext(Conversation conversation) {
                    view.moveToChatScreen(conversation);
                }

                @Override
                public void onError(@NotNull Throwable exception) {
                }
            }, group.conversationID);
        } else {
            view.showLoading();
            createGroupConversationUseCase.execute(new DefaultObserver<Conversation>() {
                @Override
                public void onNext(Conversation s) {
                    view.hideLoading();
                    view.moveToChatScreen(s);
                }

                @Override
                public void onError(@NotNull Throwable exception) {
                    view.hideLoading();
                }
            }, group);
        }
    }

    @Override
    public void destroy() {
        view = null;
        observeGroupsUseCase.dispose();
        createGroupConversationUseCase.dispose();
    }
}

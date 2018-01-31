package com.ping.android.presentation.presenters.impl;

import android.os.Handler;
import android.os.Looper;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.GetConversationUseCase;
import com.ping.android.domain.usecase.group.AddGroupMembersUseCase;
import com.ping.android.model.Conversation;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.ConversationGroupDetailPresenter;
import com.ping.android.presentation.presenters.ConversationPVPDetailPresenter;
import com.ping.android.ultility.CommonMethod;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static org.jivesoftware.smackx.privacy.packet.PrivacyItem.Type.group;

/**
 * Created by tuanluong on 1/31/18.
 */

public class ConversationGroupDetailPresenterImpl implements ConversationGroupDetailPresenter {
    @Inject
    GetConversationUseCase getConversationUseCase;
    @Inject
    AddGroupMembersUseCase addGroupMembersUseCase;
    @Inject
    View view;
    private Conversation conversation;

    @Inject
    public ConversationGroupDetailPresenterImpl() {}

    @Override
    public void initConversation(String conversationId) {
        view.showLoading();
        getConversationUseCase.execute(new DefaultObserver<Conversation>() {
            @Override
            public void onNext(Conversation data) {
                conversation = data;
                view.updateConversation(data);
                view.hideLoading();
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
                view.hideLoading();
            }
        }, conversationId);
    }

    @Override
    public void addUsersToGroup(List<User> selectedUsers) {
        List<String> ret = new ArrayList<>();
        for (User user : selectedUsers) {
            if (!conversation.group.memberIDs.containsKey(user.key)
                    || CommonMethod.isTrueValue(conversation.group.deleteStatuses, user.key)) {
                ret.add(user.key);
            }
        }
        addGroupMembersUseCase.execute(new DefaultObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                if (aBoolean) {

                }
            }
        }, new AddGroupMembersUseCase.Params(conversation.groupID, ret));
    }

    @Override
    public void destroy() {
        getConversationUseCase.dispose();
        addGroupMembersUseCase.dispose();
    }
}

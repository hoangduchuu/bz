package com.ping.android.domain.usecase.group;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.domain.repository.GroupRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.model.Conversation;
import com.ping.android.model.Group;
import com.ping.android.model.User;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 1/31/18.
 */

public class AddGroupMembersUseCase extends UseCase<List<User>, AddGroupMembersUseCase.Params> {
    @Inject
    CommonRepository commonRepository;
    @Inject
    UserRepository userRepository;

    @Inject
    public AddGroupMembersUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<List<User>> buildUseCaseObservable(AddGroupMembersUseCase.Params params) {
        Conversation conversation = params.conversation;
        List<String> userIds = params.userIds;
        Map<String, Object> updateValue = new HashMap<>();
        Group group = conversation.group;
        Map<String, Boolean> oldGroupMembers = new HashMap<>(group.memberIDs);
        // Reset delete status for group & conversation
        for (String userId: userIds) {
            group.memberIDs.put(userId, true);
            group.deleteStatuses.put(userId, null);
        }
        conversation.memberIDs = group.memberIDs;
        conversation.deleteStatuses = group.deleteStatuses;
        // Update group
//        updateValue.put(String.format("groups/%s", group.key), group.toMap());
//        updateValue.put(String.format("conversations/%s/memberIDs", conversation.key), conversation.memberIDs);
//        updateValue.put(String.format("conversations/%s/deleteStatuses", conversation.key), conversation.deleteStatuses);
        for (String userId: oldGroupMembers.keySet()) {
            updateValue.put(String.format("groups/%s/%s", userId, group.key), group.toMap());
            updateValue.put(String.format("conversations/%s/%s/memberIDs", userId, conversation.key), conversation.memberIDs);
            updateValue.put(String.format("conversations/%s/%s/deleteStatuses", userId, conversation.key), conversation.deleteStatuses);
        }
        // Create conversation for new member with empty message
        conversation.message = "";
        for (String userId : userIds) {
            updateValue.put(String.format("groups/%s/%s", userId, group.key), group.toMap());
            updateValue.put(String.format("conversations/%s/%s", userId, conversation.key), conversation.toMap());
        }
        return commonRepository.updateBatchData(updateValue)
                .flatMap(aBoolean -> userRepository.getUserList(group.memberIDs));
    }

    public static class Params {
        public Conversation conversation;
        public List<String> userIds;

        public Params(Conversation conversation, List<String> ret) {
            this.conversation = conversation;
            this.userIds = ret;
        }
    }
}

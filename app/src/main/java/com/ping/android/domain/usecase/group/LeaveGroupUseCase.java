package com.ping.android.domain.usecase.group;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.domain.repository.GroupRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Group;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/1/18.
 */

public class LeaveGroupUseCase extends UseCase<Boolean, Group> {
    @Inject
    CommonRepository commonRepository;
    @Inject
    UserRepository userRepository;

    @Inject
    public LeaveGroupUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Group group) {
        return userRepository.getCurrentUser()
                .flatMap(user -> {
                    String userId = user.key;
                    Map<String, Object> updateValue = new HashMap<>();
                    group.memberIDs.remove(userId);
                    group.deleteStatuses.put(userId, true);

                    // 1. Remove group and conversation for current opponentUser
                    updateValue.put(String.format("groups/%s/%s", userId, group.key), null);
                    updateValue.put(String.format("conversations/%s/%s", userId, group.conversationID), null);

                    // 2. Update members for group & conversation
//        updateValue.put(String.format("groups/%s/deleteStatuses", group.key), group.deleteStatuses);
//        updateValue.put(String.format("conversations/%s/deleteStatuses", group.conversationID), group.deleteStatuses);
//        updateValue.put(String.format("groups/%s/memberIDs", group.key), group.memberIDs);
//        updateValue.put(String.format("conversations/%s/memberIDs", group.conversationID), group.memberIDs);
                    for (String id : group.memberIDs.keySet()) {
                        if (id.equals(userId)) continue;
                        updateValue.put(String.format("groups/%s/%s/deleteStatuses", id, group.key), group.deleteStatuses);
                        updateValue.put(String.format("conversations/%s/%s/deleteStatuses", id, group.conversationID), group.deleteStatuses);
                        updateValue.put(String.format("groups/%s/%s/memberIDs", id, group.key), group.memberIDs);
                        updateValue.put(String.format("conversations/%s/%s/memberIDs", id, group.conversationID), group.memberIDs);
                    }
                    return commonRepository.updateBatchData(updateValue);
                });
    }
}

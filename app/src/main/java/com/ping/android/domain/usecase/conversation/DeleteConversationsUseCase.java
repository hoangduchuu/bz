package com.ping.android.domain.usecase.conversation;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.model.Conversation;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/7/18.
 */

public class DeleteConversationsUseCase extends UseCase<Boolean, List<Conversation>> {
    @Inject
    CommonRepository commonRepository;
    @Inject
    UserRepository userRepository;

    @Inject
    public DeleteConversationsUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(List<Conversation> conversations) {
        return userRepository.getCurrentUser()
                .flatMap(user -> {
                    double timestamp = System.currentTimeMillis() / 1000d;
                    Map<String, Object> updateValue = new HashMap<>();
                    for (Conversation conversation : conversations) {
                        //updateValue.put(String.format("conversations/%s/deleteStatuses/%s", conversation.key, opponentUser.key), true);
                        //updateValue.put(String.format("conversations/%s/deleteTimestamps/%s", conversation.key, opponentUser.key), timestamp);
                        for (String userId : conversation.memberIDs.keySet()) {
                            updateValue.put(String.format("conversations/%s/%s/deleteStatuses/%s", userId, conversation.key, user.key), true);
                            updateValue.put(String.format("conversations/%s/%s/deleteTimestamps/%s", userId, conversation.key, user.key), timestamp);
                        }
                    }
                    return commonRepository.updateBatchData(updateValue);
                });
    }
}

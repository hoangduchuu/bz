package com.ping.android.domain.usecase.conversation;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Conversation;
import com.ping.android.model.Group;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/24/18.
 */

public class CreateGroupConversationUseCase extends UseCase<String, Group> {
    @Inject
    UserRepository userRepository;
    @Inject
    ConversationRepository conversationRepository;
    @Inject
    CommonRepository commonRepository;
    @Inject
    UserManager userManager;

    @Inject
    public CreateGroupConversationUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<String> buildUseCaseObservable(Group group) {
        return userManager.getCurrentUser()
                .zipWith(conversationRepository.getKey(), (user, s) -> {
                    Conversation conversation = Conversation.createNewGroupConversation(user.key, group);
                    conversation.key = s;
                    return conversation;
                })
                .flatMap(conversation -> {
                    Map<String, Object> updateValue = new HashMap<>();
                    //updateValue.put(String.format("conversations/%s", key), conversation.toMap());
                    for (String userKey : conversation.memberIDs.keySet()) {
                        updateValue.put(String.format("conversations/%s/%s", userKey, conversation.key), conversation.toMap());
                        updateValue.put(String.format("groups/%s/%s/conversationID", userKey, conversation.groupID), conversation.key);
                    }
                    return commonRepository.updateBatchData(updateValue)
                            .map(aBoolean -> conversation.key);
                });
    }
}

package com.ping.android.domain.usecase.conversation;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.data.mappers.ConversationMapper;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Conversation;
import com.ping.android.model.User;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 3/13/18.
 */

public class ObserveConversationValueFromExistsConversationUseCase extends UseCase<Conversation, ObserveConversationValueFromExistsConversationUseCase.Params> {
    @Inject
    ConversationRepository conversationRepository;
    @Inject
    UserManager userManager;
    @Inject
    ConversationMapper mapper;

    @Inject
    public ObserveConversationValueFromExistsConversationUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Conversation> buildUseCaseObservable(Params params) {
        return conversationRepository.observeConversationValue(params.user.key, params.conversation.key)
                .flatMap(dataSnapshot -> {
                    Conversation conversation = mapper.transform(dataSnapshot, params.user);
                    conversation.opponentUser = params.conversation.opponentUser;
                    conversation.group = params.conversation.group;
                    if (conversation.memberIDs.keySet().size() != params.conversation.memberIDs.keySet().size()) {
                        return userManager.getUserList(conversation.memberIDs)
                                .map(users -> {
                                    params.conversation.members = users;
                                    conversation.members = users;
                                    return conversation;
                                });
                    } else {
                        conversation.members = params.conversation.members;
                    }
                    return Observable.just(conversation);
                });
    }

    public static class Params {
        public Conversation conversation;
        public User user;

        public Params(Conversation conversation, User user) {
            this.conversation = conversation;
            this.user = user;
        }
    }
}

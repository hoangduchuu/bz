package com.ping.android.domain.usecase.conversation;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.GroupRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Conversation;
import com.ping.android.model.User;
import com.ping.android.ultility.Constant;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 1/31/18.
 */

public class ObserveConversationUpdateUseCase extends UseCase<Conversation, String> {
    @Inject
    ConversationRepository conversationRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    GroupRepository groupRepository;

    @Inject
    public ObserveConversationUpdateUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Conversation> buildUseCaseObservable(String s) {
        return userRepository.getCurrentUser()
                .flatMap(user -> conversationRepository.observeConversationValue(user.key, s)
                        .flatMap(dataSnapshot -> {
                            Conversation conversation = Conversation.from(dataSnapshot);
                            conversation.currentColor = conversation.getColor(user.key);
                            return userRepository.getUserList(conversation.memberIDs)
                                    .flatMap(users -> {
                                        conversation.members = users;
                                        if (conversation.conversationType == Constant.CONVERSATION_TYPE_INDIVIDUAL) {
                                            for (User user1 : users) {
                                                if (!user1.key.equals(user.key)) {
                                                    conversation.opponentUser = user1;
                                                    break;
                                                }
                                            }
                                        }
                                        return Observable.just(conversation);
                                    });
                        }));
    }
}

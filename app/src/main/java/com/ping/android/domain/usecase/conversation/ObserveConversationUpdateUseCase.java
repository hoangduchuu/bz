package com.ping.android.domain.usecase.conversation;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.data.mappers.ConversationMapper;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.GroupRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Conversation;
import com.ping.android.model.User;
import com.ping.android.utils.configs.Constant;

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
    UserManager userManager;
    @Inject
    ConversationMapper mapper;

    @Inject
    public ObserveConversationUpdateUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Conversation> buildUseCaseObservable(String s) {
        return userManager.getCurrentUser()
                .flatMap(user -> conversationRepository.observeConversationValue(user.key, s)
                        .flatMap(dataSnapshot -> {
                            Conversation conversation = mapper.transform(dataSnapshot, user);
                            return userRepository.getUserList(conversation.memberIDs)
                                    .flatMap(users -> {
                                        for (User u : users) {
                                            u.nickName = conversation.nickNames.containsKey(u.key) ? conversation.nickNames.get(u.key) : "";
                                        }
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

package com.ping.android.domain.usecase;

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

public class GetConversationUseCase extends UseCase<Conversation, String> {
    @Inject
    ConversationRepository conversationRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    GroupRepository groupRepository;
    UserManager userManager;

    @Inject
    public GetConversationUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        userManager = UserManager.getInstance();
    }

    @NotNull
    @Override
    public Observable<Conversation> buildUseCaseObservable(String s) {
        String userKey = userManager.getUser().key;
        return conversationRepository.observeConversationValue(s)
                .flatMap(dataSnapshot -> {
                    Conversation conversation = Conversation.from(dataSnapshot);
                    return userRepository.getUserList(conversation.memberIDs)
                            .flatMap(users -> {
                                conversation.members = users;
                                if (conversation.conversationType == Constant.CONVERSATION_TYPE_INDIVIDUAL) {
                                    for (User user : users) {
                                        if (!user.key.equals(userKey)) {
                                            conversation.opponentUser = user;
                                            break;
                                        }
                                    }
                                    return Observable.just(conversation);
                                } else {
                                    return groupRepository.getGroup(conversation.groupID)
                                            .map(group -> {
                                                group.members = conversation.members;
                                                conversation.group = group;
                                                return conversation;
                                            });
                                }
                            });
                });
    }
}

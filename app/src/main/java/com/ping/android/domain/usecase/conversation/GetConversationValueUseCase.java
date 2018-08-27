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
import com.ping.android.utils.CommonMethod;
import com.ping.android.utils.configs.Constant;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 1/31/18.
 */

public class GetConversationValueUseCase extends UseCase<Conversation, String> {
    @Inject
    ConversationRepository conversationRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    UserManager userManager;
    @Inject
    GroupRepository groupRepository;

    @Inject
    public GetConversationValueUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Conversation> buildUseCaseObservable(String s) {
        return userManager.getCurrentUser()
                .flatMap(user -> conversationRepository.getConversation(user.key, s)
                        .flatMap(conversation -> {
                            conversation.currentColor = conversation.getColor(user.key);
                            conversation.deleteTimestamp = CommonMethod.getDoubleFrom(conversation.deleteTimestamps, user.key);
                            return userManager.getUserList(conversation.memberIDs)
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
                                            return Observable.just(conversation);
                                        } else {
                                            return groupRepository.getGroup(user.key, conversation.groupID)
                                                    .map(group -> {
                                                        group.members = conversation.members;
                                                        conversation.group = group;
                                                        return conversation;
                                                    });
                                        }
                                    });
                        }));
    }
}

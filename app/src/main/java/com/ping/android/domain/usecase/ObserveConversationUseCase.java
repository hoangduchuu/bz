package com.ping.android.domain.usecase;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.bzzzchat.rxfirebase.events.ChildEvent;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.GroupRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.ChildData;
import com.ping.android.model.Conversation;
import com.ping.android.model.User;
import com.ping.android.ultility.Constant;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 1/28/18.
 */

public class ObserveConversationUseCase extends UseCase<ChildData<Conversation>, Void> {
    @Inject
    ConversationRepository conversationRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    GroupRepository groupRepository;
    UserManager userManager;

    @Inject
    public ObserveConversationUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        userManager = UserManager.getInstance();
    }

    @NotNull
    @Override
    public Observable<ChildData<Conversation>> buildUseCaseObservable(Void aVoid) {
        String userKey = userManager.getUser().key;
        return conversationRepository.registerConversationUpdate(userKey)
                .flatMap(childEvent -> {
                    Conversation conversation = Conversation.from(childEvent.dataSnapshot);
                    if (conversation.memberIDs.isEmpty()) {
                        return Observable.empty();
                    }
                    if (childEvent.type == ChildEvent.Type.CHILD_CHANGED) {
                        if (conversation.deleteTimestamps.containsKey(userKey)) {
                            //conversation will not show if last message time stamp less than conversation deleted time
                            if (conversation.deleteTimestamps.get(userKey) > conversation.timesstamps) {
                                ChildData<Conversation> childData = new ChildData<>();
                                childData.data = conversation;
                                childData.type = ChildEvent.Type.CHILD_REMOVED;
                                return Observable.just(childData);
                            }
                        }
                    }
                    return userRepository.getUserList(conversation.memberIDs)
                            .map(users -> {
                                conversation.members = users;
                                if (conversation.conversationType == Constant.CONVERSATION_TYPE_INDIVIDUAL) {
                                    for (User user : users) {
                                        if (!user.key.equals(userKey)) {
                                            conversation.opponentUser = user;
                                            break;
                                        }
                                    }
                                }
                                ChildData<Conversation> childData = new ChildData<>();
                                childData.data = conversation;
                                childData.type = childEvent.type;
                                return childData;
                            }).flatMap(childData -> {
                                if (childData.data.conversationType == Constant.CONVERSATION_TYPE_GROUP) {
                                    return groupRepository.getGroup(childData.data.groupID)
                                            .map(group -> {
                                                childData.data.group = group;
                                                return childData;
                                            });
                                }
                                return Observable.just(childData);
                            });
                });
    }

}

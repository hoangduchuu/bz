package com.ping.android.domain.usecase.conversation;

import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.bzzzchat.rxfirebase.database.ChildEvent;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.GroupRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.ChildData;
import com.ping.android.model.Conversation;
import com.ping.android.model.User;
import com.ping.android.utils.CommonMethod;
import com.ping.android.utils.configs.Constant;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 1/28/18.
 */

public class ObserveConversationsUseCase extends UseCase<ChildData<Conversation>, Void> {
    @Inject
    ConversationRepository conversationRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    GroupRepository groupRepository;
    UserManager userManager;

    @Inject
    public ObserveConversationsUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        userManager = UserManager.getInstance();
    }

    @NotNull
    @Override
    public Observable<ChildData<Conversation>> buildUseCaseObservable(Void aVoid) {
        return userRepository.getCurrentUser()
                .flatMap(currentUser -> conversationRepository.registerConversationsUpdate(currentUser.key)
                        .flatMap(childEvent -> {
                            Conversation conversation = Conversation.from(childEvent.dataSnapshot);
                            if (!conversation.memberIDs.containsKey(currentUser.key)) {
                                return Observable.empty();
                            }
                            conversation.deleteTimestamp = CommonMethod.getDoubleFrom(conversation.deleteTimestamps, currentUser.key);
                            if (!conversation.isValid()) {
                                ChildData<Conversation> childData = new ChildData<>();
                                childData.data = conversation;
                                childData.type = ChildEvent.Type.CHILD_REMOVED;
                                return Observable.just(childData);
                            }
                            conversation.isRead = CommonMethod.getBooleanFrom(conversation.readStatuses, currentUser.key);
                            conversation.currentColor = conversation.getColor(currentUser.key);
                            return userRepository.getUserList(conversation.memberIDs)
                                    .map(users -> {
                                        conversation.members = users;
                                        if (conversation.conversationType == Constant.CONVERSATION_TYPE_INDIVIDUAL) {
                                            for (User user : users) {
                                                if (!user.key.equals(currentUser.key)) {
                                                    conversation.opponentUser = user;
                                                    conversation.conversationAvatarUrl = user.profile;
                                                    String nickName = conversation.nickNames.get(user.key);
                                                    String conversationName = TextUtils.isEmpty(nickName) ? user.getDisplayName() : nickName;
                                                    conversation.conversationName = conversationName;
                                                    List<String> filterTextList = new ArrayList<>();
                                                    filterTextList.add(user.getDisplayName());
                                                    filterTextList.add(nickName);
                                                    conversation.filterText = TextUtils.join(" ", filterTextList);
                                                    break;
                                                }
                                            }
                                        } else {
                                            conversation.filterText = conversation.conversationName;
                                        }
                                        ChildData<Conversation> childData = new ChildData<>();
                                        childData.data = conversation;
                                        childData.type = childEvent.type;
                                        return childData;
                                    });
                        }));
    }

}

package com.ping.android.domain.usecase.conversation;

import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.data.entity.ChildData;
import com.ping.android.data.mappers.ConversationMapper;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.GroupRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Conversation;
import com.ping.android.model.User;
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
    @Inject
    UserManager userManager;
    @Inject
    ConversationMapper mapper;

    @Inject
    public ObserveConversationsUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<ChildData<Conversation>> buildUseCaseObservable(Void aVoid) {
        return userManager.getCurrentUser()
                .flatMap(currentUser -> conversationRepository.registerConversationsUpdate(currentUser.key)
                        .flatMap(childEvent -> {
                            Conversation conversation = mapper.transform(childEvent.dataSnapshot, currentUser);
                            if (!conversation.memberIDs.containsKey(currentUser.key)) {
                                return Observable.empty();
                            }
                            if (!conversation.isValid()) {
                                ChildData<Conversation> childData = new ChildData<>(conversation, ChildData.Type.CHILD_REMOVED);
                                return Observable.just(childData);
                            }
                            return userManager.getUserList(conversation.memberIDs)
                                    .map(users -> {
                                        conversation.members = users;
                                        if (conversation.conversationType == Constant.CONVERSATION_TYPE_INDIVIDUAL) {
                                            for (User user : users) {
                                                if (!user.key.equals(currentUser.key)) {
                                                    conversation.opponentUser = user;
                                                    conversation.conversationAvatarUrl = user.settings.private_profile ? "" : user.profile;
                                                    String nickName = conversation.nickNames.get(user.key);
                                                    conversation.conversationName = TextUtils.isEmpty(nickName) ? user.getDisplayName() : nickName;
                                                    List<String> filterTextList = new ArrayList<>();
                                                    filterTextList.add(user.getDisplayName());
                                                    filterTextList.add(nickName);
                                                    conversation.filterText = TextUtils.join(" ", filterTextList);
                                                    break;
                                                }
                                            }
                                            userManager.setIndividualConversation(conversation);
                                            conversation.senderName = getSenderNameFromSenderId(conversation);

                                        } else {
                                            conversation.senderName = getSenderNameFromSenderId(conversation);
                                            conversation.filterText = conversation.conversationName;
                                        }
                                        ChildData<Conversation> childData = new ChildData<>(conversation, childEvent.type);
                                        return childData;
                                    });
                        }));
    }

    private  String getSenderNameFromSenderId(Conversation conversation) {
        for (int i =0; i <= conversation.members.size();i++){
            if (conversation.senderId.equals(conversation.members.get(i).key)){
                return conversation.members.get(i).firstName;
            }
        }
        return "can not get senderName";

    }

}

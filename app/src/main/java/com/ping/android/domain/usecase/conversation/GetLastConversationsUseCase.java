package com.ping.android.domain.usecase.conversation;

import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.google.firebase.database.DataSnapshot;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 3/12/18.
 */

public class GetLastConversationsUseCase extends UseCase<List<Conversation>, Void> {
    @Inject
    ConversationRepository conversationRepository;
    @Inject
    UserRepository userRepository;

    @Inject
    public GetLastConversationsUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<List<Conversation>> buildUseCaseObservable(Void aVoid) {
        return userRepository.getCurrentUser()
                .flatMap(user -> conversationRepository.getLastConversations(user.key)
                        .flatMap(dataSnapshot -> {
                            List<Conversation> conversations = new ArrayList<>();
                            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                                for (DataSnapshot child : dataSnapshot.getChildren()) {
                                    if (!child.exists()) continue;
                                    Conversation conversation = Conversation.from(child);
                                    if (!conversation.memberIDs.containsKey(user.key)) continue;
                                    if (conversation.deleteTimestamps.containsKey(user.key)
                                            && conversation.deleteTimestamps.get(user.key) > conversation.timesstamps) {
                                        continue;
                                        //conversation will not show if last message time stamp less than conversation deleted time
                                    }
                                    boolean readStatus = CommonMethod.getBooleanFrom(conversation.readStatuses, user.key);
                                    conversation.isRead = readStatus;
                                    conversations.add(conversation);
                                }
                            }
                            return Observable.fromArray(conversations.toArray())
                                    .flatMap(object -> {
                                        Conversation conversation = (Conversation) object;
                                        if (conversation.conversationType == Constant.CONVERSATION_TYPE_INDIVIDUAL) {
                                            for (String userId : conversation.memberIDs.keySet()) {
                                                if (!user.key.equals(userId)) {
                                                    return userRepository.getUser(userId)
                                                            .map(user1 -> {
                                                                conversation.opponentUser = user1;
                                                                conversation.conversationAvatarUrl = user1.profile;
                                                                String nickName = conversation.nickNames.get(user1.key);
                                                                String conversationName = TextUtils.isEmpty(nickName) ? user1.getDisplayName() : nickName;
                                                                conversation.conversationName = conversationName;
                                                                List<String> filterTextList = new ArrayList<>();
                                                                filterTextList.add(user.getDisplayName());
                                                                filterTextList.add(nickName);
                                                                conversation.filterText = TextUtils.join(" ", filterTextList);
                                                                return conversation;
                                                            });
                                                }
                                            }
                                        } else {
                                            conversation.filterText = conversation.conversationName;
                                        }
                                        return Observable.just(conversation);
                                    })
                                    .toList()
                                    .toObservable();
                        })
                );
    }
}

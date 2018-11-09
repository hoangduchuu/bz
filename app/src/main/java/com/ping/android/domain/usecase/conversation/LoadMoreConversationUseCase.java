package com.ping.android.domain.usecase.conversation;

import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.google.firebase.database.DataSnapshot;
import com.ping.android.data.mappers.ConversationMapper;
import com.ping.android.domain.repository.ConversationRepository;
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
 * Created by tuanluong on 3/27/18.
 */

public class LoadMoreConversationUseCase extends UseCase<LoadMoreConversationUseCase.Output, Double> {
    @Inject
    ConversationRepository conversationRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    UserManager userManager;
    @Inject
    ConversationMapper mapper;

    @Inject
    public LoadMoreConversationUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Output> buildUseCaseObservable(Double params) {
        return userManager.getCurrentUser()
                .flatMap(currenetUser -> conversationRepository.loadMoreConversation(currenetUser.key, params)
                        .flatMap(dataSnapshot -> {
                            List<Conversation> conversations = new ArrayList<>();
                            double lastTimestamp = Double.MAX_VALUE;
                            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                                for (DataSnapshot child : dataSnapshot.getChildren()) {
                                    if (!child.exists()) continue;
                                    Conversation conversation = mapper.transform(child, currenetUser);
                                    if (lastTimestamp > conversation.timesstamps) {
                                        lastTimestamp = conversation.timesstamps;
                                    }
                                    if (!conversation.memberIDs.containsKey(currenetUser.key)
                                            || !conversation.isValid()) continue;
                                    conversations.add(conversation);
                                }
                            }
                            if (conversations.size() > 0) {
                                double finalLastTimestamp = lastTimestamp;
                                return Observable.fromArray(conversations.toArray())
                                        .flatMap(conversationObject -> {
                                            Conversation conversation = (Conversation) conversationObject;
                                            if (conversation.conversationType == Constant.CONVERSATION_TYPE_INDIVIDUAL) {
                                                for (String userId : conversation.memberIDs.keySet()) {
                                                    if (!currenetUser.key.equals(userId)) {
                                                        return getUser(userId)
                                                                .map(user1 -> {
                                                                    conversation.senderName = user1.firstName;
                                                                    conversation.opponentUser = user1;
                                                                    conversation.conversationAvatarUrl = user1.settings.private_profile ? "" : user1.profile;
                                                                    String nickName = conversation.nickNames.get(user1.key);
                                                                    String conversationName = TextUtils.isEmpty(nickName) ? user1.getDisplayName() : nickName;
                                                                    conversation.conversationName = conversationName;
                                                                    List<String> filterTextList = new ArrayList<>();
                                                                    filterTextList.add(currenetUser.getDisplayName());
                                                                    filterTextList.add(nickName);
                                                                    conversation.filterText = TextUtils.join(" ", filterTextList);
                                                                    return conversation;
                                                                })
                                                                .doOnNext(con -> userManager.setIndividualConversation(conversation))
                                                                ;
                                                    }
                                                    else {
                                                        conversation.senderName = currenetUser.firstName;
                                                    }
                                                }
                                            } else {
                                                for (String userId : conversation.memberIDs.keySet()) {
                                                    if (!currenetUser.key.equals(userId)) {
                                                        return getUser(userId)
                                                                .map(user1 -> {
                                                                    conversation.senderName = user1.firstName;
                                                                    return conversation;
                                                                });
                                                    }
                                                    else {
                                                        conversation.senderName = currenetUser.firstName;
                                                    }
                                                }
                                                conversation.filterText = conversation.conversationName;
                                            }
                                            return Observable.just(conversation);
                                        })
                                        .toList()
                                        .map(conversations1 -> {
                                            Output output = new Output();
                                            output.conversations = conversations1;
                                            output.canLoadMore = dataSnapshot.getChildrenCount() == 15;
                                            output.lastTimestamp = finalLastTimestamp;
                                            return output;
                                        })
                                        .toObservable();
                            } else {
                                Output output = new Output();
                                output.conversations = new ArrayList<>();
                                output.canLoadMore = false;
                                return Observable.just(output);
                            }
                        })
                );
    }

    private Observable<User> getUser(String userId) {
        User user = userManager.getCacheUser(userId);
        if (user != null) {
            return Observable.just(user);
        }
        return userRepository.getUser(userId);
    }

    public static class Output {
        public List<Conversation> conversations;
        public boolean canLoadMore;
        public double lastTimestamp;
    }
}

package com.ping.android.domain.usecase;

import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.data.entity.CallEntity;
import com.ping.android.domain.mapper.CallMapper;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Call;
import com.ping.android.data.entity.ChildData;
import com.ping.android.model.User;
import com.ping.android.utils.configs.Constant;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 1/30/18.
 */

public class ObserveCallUseCase extends UseCase<ChildData<Call>, Void> {
    @Inject
    UserRepository userRepository;
    @Inject
    UserManager userManager;
    @Inject
    ConversationRepository conversationRepository;
    @Inject
    CallMapper mapper;
    private Map<String, String> cachedNicknames = new HashMap<>();

    @Inject
    public ObserveCallUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<ChildData<Call>> buildUseCaseObservable(Void aVoid) {
        return userManager.getCurrentUser()
                .flatMap(currentUser -> {
                    String userId = currentUser.key;
                    return userRepository.observeCalls(userId)
                            .flatMap(childData -> {
                                CallEntity entity = childData.getData();
                                Call call = mapper.transform(entity, currentUser);
                                ChildData.Type type = entity.getDeleteStatuses().containsKey(userId) && entity.getDeleteStatuses().get(userId)
                                        ? ChildData.Type.CHILD_REMOVED : ChildData.Type.CHILD_ADDED;
                                if (childData.getType() == ChildData.Type.CHILD_ADDED && type == ChildData.Type.CHILD_ADDED) {
                                    String opponentUserId = userId.equals(call.senderId) ? call.receiveId : call.senderId;
                                    return userManager.getUser(opponentUserId)
                                            .zipWith(conversationRepository.getConversationNickName(userId, call.conversationId, opponentUserId), ((user, nickName) -> {
                                                call.opponentUser = user;
                                                call.opponentName = TextUtils.isEmpty(nickName) ? call.opponentUser.getDisplayName() : nickName;
                                                return new ChildData<>(call, childData.getType());
                                            }));

                                } else {
                                    return Observable.just(new ChildData<>(call, childData.getType()));
                                }
                            });
                });

    }

    private Observable<String> getNickname(String userId, String conversationId, String opponentUserId) {
        if (cachedNicknames.containsKey(conversationId)) {
            return Observable.just(cachedNicknames.get(conversationId));
        }
        return conversationRepository.getConversationNickName(userId, conversationId, opponentUserId)
                .doOnNext(s -> cachedNicknames.put(conversationId, s));
    }
}

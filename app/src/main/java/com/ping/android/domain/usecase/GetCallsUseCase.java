package com.ping.android.domain.usecase;

import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.data.entity.ChildData;
import com.ping.android.domain.mapper.CallMapper;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Call;
import com.ping.android.model.User;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 1/30/18.
 */

public class GetCallsUseCase extends UseCase<List<Call>, Void> {
    @Inject
    UserRepository userRepository;
    @Inject
    ConversationRepository conversationRepository;
    @Inject
    CallMapper mapper;
    @Inject
    UserManager userManager;

    private Map<String, String> cachedNicknames = new HashMap<>();

    @Inject
    public GetCallsUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<List<Call>> buildUseCaseObservable(Void aVoid) {
        return userManager.getCurrentUser()
                .flatMap(currentUser -> {
                    String userId = currentUser.key;
                    return userRepository.getCalls(userId)
                            .flatMap(entities -> {
                                List<Call> calls = mapper.transform(entities, currentUser);
                                Call[] callsArray = new Call[calls.size()];
                                return Observable.fromArray(calls.toArray(callsArray))
                                        .flatMap(call -> {
                                            String opponentUserId = userId.equals(call.senderId) ? call.receiveId : call.senderId;
                                            return userManager.getUser(opponentUserId)
                                                    .zipWith(getNickname(userId, call.conversationId, opponentUserId), ((user, nickName) -> {
                                                        call.opponentUser = user;
                                                        call.opponentName = TextUtils.isEmpty(nickName) ? call.opponentUser.getDisplayName() : nickName;
                                                        return call;
                                                    }));
                                        })
                                        .take(calls.size())
                                        .toList()
                                        .toObservable();
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

package com.ping.android.domain.usecase.call;

import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.google.firebase.database.DataSnapshot;
import com.ping.android.data.entity.CallEntity;
import com.ping.android.domain.mapper.CallMapper;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Call;
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

public class LoadMoreCallUseCase extends UseCase<LoadMoreCallUseCase.Output, Double> {
    @Inject
    UserRepository userRepository;
    @Inject
    ConversationRepository conversationRepository;
    @Inject
    CallMapper callMapper;
    @Inject
    UserManager userManager;

    @Inject
    public LoadMoreCallUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Output> buildUseCaseObservable(Double params) {
        return userManager.getCurrentUser()
                .flatMap(user -> userRepository.loadMoreCalls(user.key, params)
                        .flatMap(callEntities -> {
                            List<Call> callList = new ArrayList<>();
                            double lastTimestamp = Double.MAX_VALUE;
                            for (CallEntity entity : callEntities) {
                                if (lastTimestamp > entity.getTimestamp()) {
                                    lastTimestamp = entity.getTimestamp();
                                }
                                callList.add(callMapper.transform(entity, user));
                            }
                            if (callList.size() > 0) {
                                double finalLastTimestamp = lastTimestamp;
                                return Observable.fromArray(callList.toArray())
                                        .flatMap(object -> {
                                            Call call = (Call) object;
                                            String opponentUserId = user.key.equals(call.senderId) ? call.receiveId : call.senderId;
                                            return getUser(opponentUserId)
                                                    .map(opponentUser -> {
                                                        call.opponentUser = opponentUser;
                                                        return call;
                                                    })
                                                    .flatMap(call1 -> {
                                                        String conversationID = user.key.compareTo(opponentUserId) > 0 ? user.key + opponentUserId : opponentUserId + user.key;
                                                        call.conversationId = conversationID;
                                                        return conversationRepository.getConversationNickName(user.key, conversationID, opponentUserId)
                                                                .map(nickName -> {
                                                                    call.opponentName = TextUtils.isEmpty(nickName) ? call.opponentUser.getDisplayName() : nickName;
                                                                    return call;
                                                                });
                                                    });
                                        })
                                        .toList()
                                        .map(calls -> {
                                            Output output = new Output();
                                            output.callList = calls;
                                            output.canLoadMore = callEntities.size() == 15;
                                            output.lastTimestamp = finalLastTimestamp;
                                            return output;
                                        })
                                        .toObservable();
                            } else {
                                Output output = new Output();
                                output.callList = new ArrayList<>();
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
        public List<Call> callList;
        public boolean canLoadMore;
        public double lastTimestamp;
    }
}

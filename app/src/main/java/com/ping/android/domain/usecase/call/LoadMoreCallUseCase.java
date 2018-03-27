package com.ping.android.domain.usecase.call;

import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.google.firebase.database.DataSnapshot;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.model.Call;
import com.ping.android.model.User;
import com.ping.android.ultility.Constant;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public LoadMoreCallUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Output> buildUseCaseObservable(Double params) {
        return userRepository.getCurrentUser()
                .flatMap(user -> userRepository.loadMoreCalls(user.key, params)
                        .flatMap(dataSnapshot -> {
                            List<Call> callList = new ArrayList<>();
                            double lastTimestamp = Double.MAX_VALUE;
                            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                                for (DataSnapshot child : dataSnapshot.getChildren()) {
                                    if (!child.exists()) continue;
                                    Call call = Call.from(child);
                                    if (lastTimestamp > call.timestamp) {
                                        lastTimestamp = call.timestamp;
                                    }
                                    if (call.status == Constant.CALL_STATUS_SUCCESS) {
                                        if (call.senderId.equals(user.key)) {
                                            call.type = Call.CallType.OUTGOING;
                                        } else {
                                            call.type = Call.CallType.INCOMING;
                                        }
                                    } else {
                                        if (call.senderId.equals(user.key)) {
                                            call.type = Call.CallType.OUTGOING;
                                        } else {
                                            call.type = Call.CallType.MISSED;
                                        }
                                    }
                                    callList.add(call);
                                }
                            }
                            if (callList.size() > 0) {
                                double finalLastTimestamp = lastTimestamp;
                                return Observable.fromArray(callList.toArray())
                                        .flatMap(object -> {
                                            Call call = (Call) object;
                                            Map<String, Boolean> memberIDs = new HashMap<>();
                                            memberIDs.put(call.senderId, true);
                                            memberIDs.put(call.receiveId, true);
                                            return userRepository.getUserList(memberIDs)
                                                    .map(users -> {
                                                        call.members = users;
                                                        for (User u : call.members) {
                                                            if (!u.key.equals(user.key)) {
                                                                call.opponentUser = u;
                                                                break;
                                                            }
                                                        }
                                                        return call;
                                                    })
                                                    .flatMap(call1 -> {
                                                        String opponentUserId = user.key.equals(call.senderId) ? call.receiveId : call.senderId;
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
                                            output.canLoadMore = dataSnapshot.getChildrenCount() == 15;
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

    public static class Output {
        public List<Call> callList;
        public boolean canLoadMore;
        public double lastTimestamp;
    }
}

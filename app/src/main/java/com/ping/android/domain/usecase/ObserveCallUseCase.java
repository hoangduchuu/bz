package com.ping.android.domain.usecase;

import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.bzzzchat.rxfirebase.database.ChildEvent;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Call;
import com.ping.android.model.ChildData;
import com.ping.android.model.User;

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
    ConversationRepository conversationRepository;
    UserManager userManager;

    @Inject
    public ObserveCallUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        userManager = UserManager.getInstance();
    }

    @NotNull
    @Override
    public Observable<ChildData<Call>> buildUseCaseObservable(Void aVoid) {
        String userId = userManager.getUser().key;
        return userRepository.getCalls(userId)
                .flatMap(childEvent -> {
                    Call call = Call.from(childEvent.dataSnapshot);
                    ChildEvent.Type type = call.deleteStatuses.containsKey(userId) && call.deleteStatuses.get(userId)
                            ? ChildEvent.Type.CHILD_REMOVED : ChildEvent.Type.CHILD_ADDED;
                    if (type == ChildEvent.Type.CHILD_ADDED) {
                        String opponentUserId = userId.equals(call.senderId) ? call.receiveId : call.senderId;
                        String conversationID = userId.compareTo(opponentUserId) > 0 ? userId + opponentUserId : opponentUserId + userId;
                        call.conversationId = conversationID;
                        Map<String, Boolean> memberIDs = new HashMap<>();
                        memberIDs.put(call.senderId, true);
                        memberIDs.put(call.receiveId, true);
                        return userRepository.getUserList(memberIDs)
                                .map(users -> {
                                    call.members = users;
                                    for (User user : call.members) {
                                        if (!user.key.equals(userId)) {
                                            call.opponentUser = user;
                                            break;
                                        }
                                    }
                                    return call;
                                })
                                .flatMap(call1 -> conversationRepository.getConversationNickName(userId, conversationID, opponentUserId)
                                        .map(nickName -> {
                                            call.opponentName = TextUtils.isEmpty(nickName) ? call.opponentUser.getDisplayName() : nickName;
                                            return new ChildData<>(call, type);
                                        }));
                    } else {
                        return Observable.just(new ChildData<>(call, type));
                    }
                });
    }
}

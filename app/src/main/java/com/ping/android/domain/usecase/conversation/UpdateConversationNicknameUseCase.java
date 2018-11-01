package com.ping.android.domain.usecase.conversation;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.model.Conversation;
import com.ping.android.model.Nickname;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 3/21/18.
 */

public class UpdateConversationNicknameUseCase extends UseCase<Boolean, UpdateConversationNicknameUseCase.Params> {
    @Inject
    CommonRepository commonRepository;

    @Inject
    public UpdateConversationNicknameUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Params params) {
        Map<String, Object> updateValue = new HashMap<>();
        //updateValue.put(String.format("conversations/%s/nickNames/%s", conversation, nickname.userId), nickname.opponentNickname);
        for (String userId : params.conversation.memberIDs.keySet()) {
            updateValue.put(String.format("conversations/%s/%s/nickNames/%s", userId,
                    params.conversation.key, params.nickname.userId), params.nickname.nickName);
        }
        return commonRepository.updateBatchData(updateValue);
    }

    public static class Params {
        public final Conversation conversation;
        public final Nickname nickname;

        public Params(Conversation conversation, Nickname nickname) {
            this.conversation = conversation;
            this.nickname = nickname;
        }
    }
}

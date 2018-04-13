package com.ping.android.domain.usecase.conversation;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.model.Conversation;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

public class UpdateConversationColorUseCase extends UseCase<Boolean, UpdateConversationColorUseCase.Params> {
    @Inject
    CommonRepository commonRepository;

    @Inject
    public UpdateConversationColorUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Params params) {
        Map<String, Object> updateValue = new HashMap<>();
        for (String userId : params.conversation.memberIDs.keySet()) {
            updateValue.put(String.format("conversations/%s/%s/themes/%s/mainColor",
                    userId, params.conversation.key, params.userId), params.color);
        }
        return commonRepository.updateBatchData(updateValue);
    }

    public static class Params {
        private final int color;
        private final String userId;
        private final Conversation conversation;

        public Params(String userId, Conversation conversation, int color) {
            this.userId = userId;
            this.conversation = conversation;
            this.color = color;
        }
    }
}

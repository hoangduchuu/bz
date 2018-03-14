package com.ping.android.domain.usecase.conversation;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.model.Conversation;
import com.ping.android.model.User;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 3/14/18.
 */

public class ToggleConversationTypingUseCase extends UseCase<Boolean, ToggleConversationTypingUseCase.Params> {
    @Inject
    CommonRepository commonRepository;

    @Inject
    public ToggleConversationTypingUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Params params) {
        Map<String, Object> updateData = new HashMap<>();
        for (User user: params.conversation.members) {
            updateData.put(String.format("conversations/%s/%s/typingIndicator/%s",
                    user.key, params.conversation.key, params.userId), params.value);
        }
        return commonRepository.updateBatchData(updateData);
    }

    public static class Params {
        public final String userId;
        public final Conversation conversation;
        public final boolean value;

        public Params(String userId, Conversation conversation, boolean value) {
            this.userId = userId;
            this.conversation = conversation;
            this.value = value;
        }
    }
}

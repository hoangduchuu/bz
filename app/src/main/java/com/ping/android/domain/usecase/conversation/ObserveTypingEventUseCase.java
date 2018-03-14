package com.ping.android.domain.usecase.conversation;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.ConversationRepository;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 3/13/18.
 */

public class ObserveTypingEventUseCase extends UseCase<Map<String, Boolean>, ObserveTypingEventUseCase.Params> {
    @Inject
    ConversationRepository conversationRepository;

    @Inject
    public ObserveTypingEventUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Map<String, Boolean>> buildUseCaseObservable(Params params) {
        return conversationRepository.observeTypingEvent(params.conversationId, params.userId);
    }

    public static class Params {
        public final String conversationId;
        public final String userId;

        public Params(String conversationId, String userId) {
            this.conversationId = conversationId;
            this.userId = userId;
        }
    }
}

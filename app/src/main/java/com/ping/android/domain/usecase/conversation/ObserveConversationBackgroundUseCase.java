package com.ping.android.domain.usecase.conversation;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.ConversationRepository;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

public class ObserveConversationBackgroundUseCase extends UseCase<String, ObserveConversationBackgroundUseCase.Params> {
    @Inject
    ConversationRepository conversationRepository;

    @Inject
    public ObserveConversationBackgroundUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<String> buildUseCaseObservable(Params params) {
        return conversationRepository
                .observeConversationBackground(params.userId, params.conversationId);
    }

    public static class Params {

        private String conversationId;
        private String userId;

        public Params(String conversationId, String userId) {
            this.conversationId = conversationId;
            this.userId = userId;
        }
    }
}

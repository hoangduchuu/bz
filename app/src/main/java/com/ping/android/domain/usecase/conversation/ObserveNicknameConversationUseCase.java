package com.ping.android.domain.usecase.conversation;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.ConversationRepository;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

public class ObserveNicknameConversationUseCase extends UseCase<Map<String, String>, ObserveNicknameConversationUseCase.Params> {
    @Inject
    ConversationRepository conversationRepository;
    
    @Inject
    public ObserveNicknameConversationUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Map<String, String>> buildUseCaseObservable(Params params) {
        return conversationRepository.observeNicknames(params.userId, params.conversationId);
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

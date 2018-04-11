package com.ping.android.domain.usecase.message;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.MessageRepository;
import com.ping.android.domain.repository.UserRepository;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

public class UpdateMessageStatusUseCase extends UseCase<Boolean, UpdateMessageStatusUseCase.Params> {
    @Inject
    MessageRepository messageRepository;
    @Inject
    UserRepository userRepository;

    @Inject
    public UpdateMessageStatusUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Params params) {
        return userRepository.getCurrentUser()
        .flatMap(user -> messageRepository.updateMessageStatus(params.conversationId, params.messageId, user.key, params.status));
    }

    public static class Params {
        private String conversationId;
        private int status;
        private String messageId;

        public Params(String conversationId, int status, String messageId) {
            this.conversationId = conversationId;
            this.status = status;
            this.messageId = messageId;
        }
    }
}

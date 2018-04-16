package com.ping.android.domain.usecase.conversation;

import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.StorageRepository;
import com.ping.android.model.Conversation;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

public class UploadConversationBackgroundUseCase extends UseCase<Boolean, UploadConversationBackgroundUseCase.Params> {
    @Inject
    StorageRepository storageRepository;
    @Inject
    UpdateConversationBackgroundUseCase updateConversationBackgroundUseCase;

    @Inject
    public UploadConversationBackgroundUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Params params) {
        if (TextUtils.isEmpty(params.filePath)) return Observable.just(false);
        return storageRepository.uploadImageMessage(params.conversation.key, params.filePath)
                .flatMap(s -> updateConversationBackgroundUseCase.buildUseCaseObservable(
                        new UpdateConversationBackgroundUseCase.Params(params.conversation, s))
                );
    }

    public static class Params {
        private String filePath;
        private Conversation conversation;

        public Params(String filePath, Conversation conversation) {
            this.filePath = filePath;
            this.conversation = conversation;
        }
    }
}

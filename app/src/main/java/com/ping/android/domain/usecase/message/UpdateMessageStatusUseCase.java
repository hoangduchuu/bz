package com.ping.android.domain.usecase.message;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.utils.configs.Constant;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

public class UpdateMessageStatusUseCase extends UseCase<Boolean, UpdateMessageStatusUseCase.Params> {
    @Inject
    CommonRepository commonRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    UserManager userManager;

    @Inject
    public UpdateMessageStatusUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Params params) {
        return userManager.getCurrentUser()
        .flatMap(user -> {
            Map<String, Object> updateValue = new HashMap<>();
            updateValue.put(String.format("messages/%s/%s/status/%s", params.conversationId, params.messageId, user.key), params.status);
            if (params.messageType == Constant.MSG_TYPE_GAME) {
                updateValue.put(String.format("media/%s/%s/status/%s", params.conversationId, params.messageId, user.key), params.status);
            }
            return commonRepository.updateBatchData(updateValue);
        });
    }

    public static class Params {
        private String conversationId;
        private int status;
        private String messageId;
        private int messageType;

        public Params(String conversationId, int status, String messageId, int messageType) {
            this.conversationId = conversationId;
            this.status = status;
            this.messageId = messageId;
            this.messageType = messageType;
        }
    }
}

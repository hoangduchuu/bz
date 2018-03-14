package com.ping.android.domain.usecase.message;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.model.Message;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 3/13/18.
 */

public class UpdateMaskMessagesUseCase extends UseCase<Boolean, UpdateMaskMessagesUseCase.Params> {
    @Inject
    UserRepository userRepository;
    @Inject
    CommonRepository commonRepository;

    @Inject
    public UpdateMaskMessagesUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Params params) {
        return userRepository.getCurrentUser()
                .flatMap(user -> {
                    Map<String, Object> updateValue = new HashMap<>();
                    for (Message message : params.messages) {
                        updateValue.put(String.format("messages/%s/%s/markStatuses/%s", params.conversationId,
                                message.key, user.key), params.isMask);
                    }
                    if (params.isLastMessage) {
                        updateValue.put(String.format("conversations/%s/%s/markStatuses/%s/", user.key,
                                params.conversationId, user.key), params.isMask);
                    }
                    return commonRepository.updateBatchData(updateValue);
                });
    }

    public static class Params {
        public List<Message> messages;
        public String conversationId;
        public boolean isLastMessage;
        public boolean isMask;
    }
}

package com.ping.android.domain.usecase.message;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.domain.repository.MessageRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;
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

public class DeleteMessagesUseCase extends UseCase<Boolean, DeleteMessagesUseCase.Params> {
    @Inject
    UserRepository userRepository;
    @Inject
    CommonRepository commonRepository;
    @Inject
    MessageRepository messageRepository;
    @Inject
    UserManager userManager;

    @Inject
    public DeleteMessagesUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Params params) {
        return userManager.getCurrentUser()
                .flatMap(user -> {
                    Map<String, Object> updateValue = new HashMap<>();
                    for (Message message : params.messages) {
                        updateValue.put(String.format("messages/%s/%s/deleteStatuses/%s", params.conversationId,
                                message.key, user.key), true);
                        updateValue.put(String.format("media/%s/%s/deleteStatuses/%s", params.conversationId,
                                message.key, user.key), true);
                        updateValue.put(String.format("messages/%s/%s/updateAt", params.conversationId,
                                message.key, user.key), System.currentTimeMillis()/1000d);
                    }
                    return commonRepository.updateBatchData(updateValue)
                            .doOnNext(aBoolean -> {
                                for (Message message: params.messages) {
                                    messageRepository.deleteCacheMessage(message.key);
                                }
                            });
                });
    }

    public static class Params {
        public String conversationId;
        public List<Message> messages;
    }
}

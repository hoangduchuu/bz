package com.ping.android.domain.usecase.message;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Message;
import com.ping.android.model.enums.MessageType;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 3/13/18.
 */

public class UpdateMaskChildMessagesUseCase extends UseCase<Boolean, UpdateMaskChildMessagesUseCase.Params> {
    @Inject
    UserRepository userRepository;
    @Inject
    CommonRepository commonRepository;
    @Inject
    UserManager userManager;

    @Inject
    public UpdateMaskChildMessagesUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Params params) {
        return userManager.getCurrentUser()
                .flatMap(user -> {
                    Map<String, Object> updateValue = new HashMap<>();
                    for (Message child : params.messages) {
                        updateValue.put(String.format("messages/%s/%s/childMessages/%s/markStatuses/%s",
                                params.conversationId, child.parentKey,
                                child.key, user.key), params.isMask);

                        if (child.type == MessageType.IMAGE || child.type == MessageType.GAME) {
                            updateValue.put(String.format("media/%s/%s/childMessages/%s/markStatuses/%s",
                                    params.conversationId, child.parentKey, child.key, user.key), params.isMask);
                        }
                        updateValue.put(String.format("messages/%s/%s/childMessages/%s/updateAt",
                                params.conversationId, child.parentKey, child.key), System.currentTimeMillis()/1000d);
                        updateValue.put(String.format("messages/%s/%s/updateAt",
                                params.conversationId, child.parentKey), System.currentTimeMillis()/1000d);
                    }
                    return commonRepository.updateBatchData(updateValue);
                });
    }

    public static class Params {
        public List<Message> messages;
        public String conversationId;
        public boolean isMask;
    }
}

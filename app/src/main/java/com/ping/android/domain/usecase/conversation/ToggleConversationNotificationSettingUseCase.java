package com.ping.android.domain.usecase.conversation;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.domain.repository.UserRepository;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/1/18.
 */

public class ToggleConversationNotificationSettingUseCase extends UseCase<Boolean, ToggleConversationNotificationSettingUseCase.Params> {
    @Inject
    CommonRepository commonRepository;
    @Inject
    UserRepository userRepository;

    @Inject
    public ToggleConversationNotificationSettingUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Params params) {
        return userRepository.getCurrentUser()
                .flatMap(user -> {
                    Map<String, Object> updateValue = new HashMap<>();
                    for (String userId : params.memberIds) {
                        updateValue.put(String.format("conversations/%s/%s/notifications/%s",
                                userId, params.conversationId, user.key), params.isEnable);
                    }
                    return commonRepository.updateBatchData(updateValue);
                });
    }

    public static class Params {
        public List<String> memberIds;
        public String conversationId;
        public boolean isEnable;

        public Params(String conversationId, List<String> memberIds, boolean isEnable) {
            this.memberIds = memberIds;
            this.conversationId = conversationId;
            this.isEnable = isEnable;
        }
    }
}

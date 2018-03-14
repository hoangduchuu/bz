package com.ping.android.domain.usecase.conversation;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.managers.UserManager;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/1/18.
 */

public class ToggleNotificationSettingUseCase extends UseCase<Boolean, ToggleNotificationSettingUseCase.Params> {
    @Inject
    CommonRepository commonRepository;
    UserManager userManager;

    @Inject
    public ToggleNotificationSettingUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        userManager = UserManager.getInstance();
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Params params) {
        Map<String, Object> updateValue = new HashMap<>();
        String userId = userManager.getUser().key;
        //updateValue.put(String.format("conversations/%s/notifications/%s", params.conversation, userId), params.isEnable);
        updateValue.put(String.format("conversations/%s/%s/notifications/%s", userId, params.conversationId, userId), params.isEnable);
        return commonRepository.updateBatchData(updateValue);
    }

    public static class Params {
        public String conversationId;
        public boolean isEnable;

        public Params(String conversationId, boolean isEnable) {
            this.conversationId = conversationId;
            this.isEnable = isEnable;
        }
    }
}

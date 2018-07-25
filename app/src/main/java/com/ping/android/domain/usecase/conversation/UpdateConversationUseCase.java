package com.ping.android.domain.usecase.conversation;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Conversation;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 3/8/18.
 */

public class UpdateConversationUseCase extends UseCase<Boolean, UpdateConversationUseCase.Params> {
    @Inject
    CommonRepository commonRepository;
    @Inject
    UserManager userManager;

    @Inject
    public UpdateConversationUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Params params) {
        return userManager.getCurrentUser()
                .flatMap(user -> {
                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put(String.format("conversations/%s/%s", user.key, params.conversation.key), params.conversation.toMap());
                    return commonRepository.updateBatchData(updateData);
                });
    }

    public static class Params {
        public Conversation conversation;
        public Map<String, Boolean> readAllowance;

        public Params(Conversation conversation, Map<String, Boolean> readAllowance) {
            this.conversation = conversation;
            this.readAllowance = readAllowance;
        }

    }
}

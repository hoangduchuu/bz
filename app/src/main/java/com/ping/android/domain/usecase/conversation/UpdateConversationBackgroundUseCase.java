package com.ping.android.domain.usecase.conversation;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Conversation;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

public class UpdateConversationBackgroundUseCase extends UseCase<Boolean, UpdateConversationBackgroundUseCase.Params> {
    @Inject
    CommonRepository commonRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    UserManager userManager;

    @Inject
    public UpdateConversationBackgroundUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Params params) {
        return userManager.getCurrentUser()
                .flatMap(user -> {
                    Map<String, Object> updateValue = new HashMap<>();
                    for (String userId : params.conversation.memberIDs.keySet()) {
                        updateValue.put(String.format("conversations/%s/%s/themes/%s/backgroundUrl",
                                userId, params.conversation.key, user.key), params.imageUrl);
                    }
                    return commonRepository.updateBatchData(updateValue);
                });
    }

    public static class Params {
        private Conversation conversation;
        private String imageUrl;

        public Params(Conversation conversation, String imageUrl) {
            this.conversation = conversation;
            this.imageUrl = imageUrl;
        }
    }
}

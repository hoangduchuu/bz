package com.ping.android.domain.usecase.notification;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.device.Notification;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

public class ShowIncomingMessageNotificationUseCase extends UseCase<Boolean, ShowIncomingMessageNotificationUseCase.Params> {
    @Inject
    UserRepository userRepository;
    @Inject
    Notification notification;
    @Inject
    UserManager userManager;

    @Inject
    public ShowIncomingMessageNotificationUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Params params) {
        return userManager.getCurrentUser()
                .map(user -> {
                    notification.showMessageNotification(user, params.message, params.conversationId, params.senderProfile);
                    return true;
                });
    }

    public static class Params {
        private String message;
        private String conversationId;
        private String senderProfile;

        public Params(String message, String conversationId, String senderProfile) {
            this.message = message;
            this.conversationId = conversationId;
            this.senderProfile = senderProfile;
        }
    }
}

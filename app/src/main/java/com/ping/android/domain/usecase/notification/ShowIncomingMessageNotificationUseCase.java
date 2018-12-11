package com.ping.android.domain.usecase.notification;

import android.app.Activity;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.device.Notification;
import com.ping.android.domain.repository.MessageRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.domain.usecase.RemoveUserBadgeUseCase;
import com.ping.android.managers.UserManager;
import com.ping.android.presentation.view.activity.ChatActivity;
import com.ping.android.utils.ActivityLifecycle;
import com.ping.android.utils.Log;
import com.ping.android.utils.SharedPrefsHelper;
import com.ping.android.utils.configs.Constant;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import io.reactivex.Observable;

public class ShowIncomingMessageNotificationUseCase extends UseCase<Boolean, ShowIncomingMessageNotificationUseCase.Params> {
    @Inject
    UserRepository userRepository;
    @Inject
    MessageRepository messageRepository;
    @Inject
    Notification notification;
    @Inject
    UserManager userManager;
    @Inject
    RemoveUserBadgeUseCase removeUserBadgeUseCase;
    private AtomicBoolean isSent = new AtomicBoolean(false);

    @Inject
    public ShowIncomingMessageNotificationUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Params params) {
        return userManager.getCurrentUser()
                .flatMap(user -> messageRepository.getMessageStatus(params.conversationId, params.messageId, user.key)
                        .map(messageStatus -> {
                            if ( (messageStatus == Constant.MESSAGE_STATUS_HIDE || messageStatus != Constant.MESSAGE_STATUS_READ ) && !isSent.get()) {
                                isSent.set(true);
                                notification.showMessageNotification(user, params.message, params.conversationId, params.senderProfile, params.badgeCount);
                                return true;
                            }
                            return false;
                        })
                        .flatMap(aBoolean -> {
                            if (!aBoolean) {
                                return removeUserBadgeUseCase.buildUseCaseObservable(params.conversationId);
                            }
                            return Observable.just(aBoolean);
                        })
                );
    }

    public static class Params {
        private String message;
        private String conversationId;
        private String messageId;
        private String senderProfile;
        private int badgeCount;

        public Params(String message, String conversationId, String messageId, String senderProfile, int badgeCount) {
            this.message = message;
            this.conversationId = conversationId;
            this.messageId = messageId;
            this.senderProfile = senderProfile;
            this.badgeCount = badgeCount;
        }
    }
}

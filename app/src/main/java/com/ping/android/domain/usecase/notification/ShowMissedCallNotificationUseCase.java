package com.ping.android.domain.usecase.notification;

import android.util.Pair;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.device.Notification;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.User;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

public class ShowMissedCallNotificationUseCase extends UseCase<Boolean, ShowMissedCallNotificationUseCase.Params> {
    @Inject
    UserRepository userRepository;
    @Inject
    Notification notification;
    @Inject
    UserManager userManager;

    @Inject
    public ShowMissedCallNotificationUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Params params) {
        return userManager.getCurrentUser()
                .zipWith(userRepository.getUser(params.opponentUserId), Pair::create)
                .map(userUserPair -> {
                    User currentUser = userUserPair.first;
                    User opponentUser = userUserPair.second;
                    boolean soundNotification = currentUser.settings.notification;
                    notification.showMissedCallNotification(opponentUser.key, params.opponentProfile, params.message,
                            params.isVideo, opponentUser.key, soundNotification);
                    return true;
                });
    }

    public static class Params {
        private String opponentUserId;
        private String opponentProfile;
        private String message;
        private boolean isVideo;

        public Params(String opponentUserId, String opponentProfile, String message, boolean isVideo) {
            this.opponentUserId = opponentUserId;
            this.opponentProfile = opponentProfile;
            this.message = message;
            this.isVideo = isVideo;
        }
    }
}

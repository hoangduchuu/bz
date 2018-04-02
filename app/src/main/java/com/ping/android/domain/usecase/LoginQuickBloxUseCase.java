package com.ping.android.domain.usecase;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.QuickbloxRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.model.User;
import com.quickblox.users.model.QBUser;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

public class LoginQuickBloxUseCase extends UseCase<Boolean, Void> {
    @Inject
    UserRepository userRepository;
    @Inject
    QuickbloxRepository quickbloxRepository;

    @Inject
    public LoginQuickBloxUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Void aVoid) {
        return userRepository.getCurrentUser()
                .flatMap(user -> loginWithQuickBlox(user)
                        .map(qbUser -> true));
    }

    private Observable<QBUser> loginWithQuickBlox(User user) {
        if (user.quickBloxID > 0) {
            return quickbloxRepository.signIn(user.quickBloxID, user.pingID);
        } else {
            return quickbloxRepository.signUp(user.pingID)
                    .flatMap(qbUser -> {
                        user.quickBloxID = qbUser.getId();
                        return userRepository.updateQuickbloxId(qbUser.getId())
                                .map(aBoolean1 -> qbUser);
                    });
        }
    }
}

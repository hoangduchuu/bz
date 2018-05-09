package com.ping.android.domain.usecase;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.MainThreadExecutor;
import com.ping.android.domain.repository.QuickbloxRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;
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
    UserManager userManager;

    @Inject
    public LoginQuickBloxUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(new MainThreadExecutor(), postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Void aVoid) {
        userManager = UserManager.getInstance();
        return userRepository.getCurrentUser()
                .flatMap(user -> loginWithQuickBlox(user)
                        .map(qbUser -> {
                            user.quickBloxID = qbUser.getId();
                            userManager.setUser(user);
                            return true;
                        }));
    }

    private Observable<QBUser> loginWithQuickBlox(User user) {
        return quickbloxRepository.getUser(user.pingID).flatMap(qbUser-> quickbloxRepository.signIn(user.quickBloxID, user.pingID)).onErrorResumeNext(error->{
            return quickbloxRepository.signUp(user.pingID)
                    .flatMap(qbUser -> {
                        user.quickBloxID = qbUser.getId();
                        return userRepository.updateQuickbloxId(qbUser.getId())
                                .flatMap(aBoolean1 -> quickbloxRepository.signIn(user.quickBloxID, user.pingID));
                    });
        });
    }
}

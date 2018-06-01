package com.ping.android.domain.usecase.group;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.GroupRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Group;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/27/18.
 */

public class ObserveGroupValueUseCase extends UseCase<Group, String> {
    @Inject
    GroupRepository groupRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    UserManager userManager;

    @Inject
    public ObserveGroupValueUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Group> buildUseCaseObservable(String s) {
        return userManager.getCurrentUser()
                .flatMap(user -> groupRepository.observeGroupValue(user.key, s));
    }
}

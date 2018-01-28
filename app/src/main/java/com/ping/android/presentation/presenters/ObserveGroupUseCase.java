package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.GroupRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.ChildData;
import com.ping.android.model.Group;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 1/28/18.
 */

public class ObserveGroupUseCase extends UseCase<ChildData<Group>, String> {

    @Inject
    GroupRepository groupRepository;
    UserManager userManager;

    @Inject
    public ObserveGroupUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        userManager = UserManager.getInstance();
    }

    @NotNull
    @Override
    public Observable<ChildData<Group>> buildUseCaseObservable(String s) {
        String userId = userManager.getUser().key;
        return groupRepository.groupsChange(userId)
                .map(childEvent -> {
                    Group group = Group.from(childEvent.dataSnapshot);
                    return new ChildData<>(group, childEvent.type);
                });
    }
}

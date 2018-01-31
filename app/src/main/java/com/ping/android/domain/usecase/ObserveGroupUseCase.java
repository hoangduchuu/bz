package com.ping.android.domain.usecase;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.GroupRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.ChildData;
import com.ping.android.model.Group;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 1/28/18.
 */

public class ObserveGroupUseCase extends UseCase<ChildData<Group>, ObserveGroupUseCase.Params> {
    @Inject
    GroupRepository groupRepository;
    @Inject
    UserRepository userRepository;
    UserManager userManager;

    @Inject
    public ObserveGroupUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        userManager = UserManager.getInstance();
    }

    @NotNull
    @Override
    public Observable<ChildData<Group>> buildUseCaseObservable(Params params) {
        String userId = userManager.getUser().key;
        return groupRepository.groupsChange(userId)
                .flatMap(childEvent -> {
                    Group group = Group.from(childEvent.dataSnapshot);
                    if (params.initUsers) {
                        return userRepository.getUserList(group.memberIDs)
                                .map(users -> {
                                    group.members = users;
                                    return new ChildData<>(group, childEvent.type);
                                });
                    } else {
                        ChildData<Group> childData = new ChildData<>(group, childEvent.type);
                        return Observable.just(childData);
                    }
                });
    }

    public static class Params {
        public boolean initUsers;

        public Params(boolean initUsers) {
            this.initUsers = initUsers;
        }
    }
}

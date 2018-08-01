package com.ping.android.domain.usecase;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.GroupRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.data.entity.ChildData;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Group;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 1/28/18.
 */

public class ObserveGroupsUseCase extends UseCase<ChildData<Group>, ObserveGroupsUseCase.Params> {
    @Inject
    GroupRepository groupRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    UserManager userManager;

    @Inject
    public ObserveGroupsUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<ChildData<Group>> buildUseCaseObservable(Params params) {
        return userManager.getCurrentUser()
                .flatMap(user -> groupRepository.groupsChange(user.key)
                        .flatMap(childEvent -> {
                            Group group = Group.from(childEvent.dataSnapshot);
                            if (params.initUsers) {
                                return userManager.getUserList(group.memberIDs)
                                        .map(users -> {
                                            group.members = users;
                                            return new ChildData<>(group, childEvent.type);
                                        });
                            } else {
                                ChildData<Group> childData = new ChildData<>(group, childEvent.type);
                                return Observable.just(childData);
                            }
                        }));
    }

    public static class Params {
        public boolean initUsers;

        public Params(boolean initUsers) {
            this.initUsers = initUsers;
        }
    }
}

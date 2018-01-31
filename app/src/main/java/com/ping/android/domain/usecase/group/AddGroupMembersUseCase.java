package com.ping.android.domain.usecase.group;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.GroupRepository;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 1/31/18.
 */

public class AddGroupMembersUseCase extends UseCase<Boolean, AddGroupMembersUseCase.Params> {
    @Inject
    GroupRepository groupRepository;

    @Inject
    public AddGroupMembersUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(AddGroupMembersUseCase.Params params) {
        return groupRepository.addUsersToGroup(params.groupId, params.userIds);
    }

    public static class Params {
        public String groupId;
        public List<String> userIds;

        public Params(String groupID, List<String> ret) {
            this.groupId = groupID;
            this.userIds = ret;
        }
    }
}

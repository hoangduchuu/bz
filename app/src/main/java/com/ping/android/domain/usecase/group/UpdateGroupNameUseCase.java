package com.ping.android.domain.usecase.group;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.CommonRepository;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/1/18.
 */

public class UpdateGroupNameUseCase extends UseCase<Boolean, UpdateGroupNameUseCase.Params> {
    @Inject
    CommonRepository commonRepository;

    @Inject
    public UpdateGroupNameUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Params params) {
        Map<String, Object> updateValue = new HashMap<>();
        updateValue.put(String.format("groups/%s/groupName", params.groupId), params.name);
        for (String userId: params.userIds) {
            updateValue.put(String.format("groups/%s/%s/groupName", userId, params.groupId), params.name);
        }
        return commonRepository.updateBatchData(updateValue);
    }

    public static class Params {
        public String groupId;
        public String name;
        public List<String> userIds;

        public Params(String groupId, String name, List<String> userIds) {
            this.groupId = groupId;
            this.name = name;
            this.userIds = userIds;
        }
    }
}

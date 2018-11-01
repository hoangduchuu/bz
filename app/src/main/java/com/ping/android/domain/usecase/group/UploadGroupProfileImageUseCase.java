package com.ping.android.domain.usecase.group;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.domain.repository.StorageRepository;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/1/18.
 */

public class UploadGroupProfileImageUseCase extends UseCase<Boolean, UploadGroupProfileImageUseCase.Params> {
    @Inject
    StorageRepository storageRepository;
    @Inject
    CommonRepository commonRepository;

    @Inject
    public UploadGroupProfileImageUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Params params) {
        return storageRepository.uploadGroupProfileImage(params.groupId, params.filePath)
                .flatMap(s -> {
                    // Should upload image url to group
                    Map<String, Object> updateValue = new HashMap<>();
                    //updateValue.put(String.format("groups/%s/groupAvatar", params.groupId), s);
                    for (String userId : params.memberIds) {
                        updateValue.put(String.format("groups/%s/%s/groupAvatar", userId, params.groupId), s);
                        updateValue.put(String.format("conversations/%s/%s/conversationAvatarUrl", userId, params.conversationId), s);
                    }
                    return commonRepository.updateBatchData(updateValue);
                });
    }

    public static class Params {
        public String filePath;
        public String groupId;
        public String conversationId;
        public List<String> memberIds;

        public Params(String groupId, String conversationId, String filePath, List<String> memberIds) {
            this.filePath = filePath;
            this.groupId = groupId;
            this.conversationId = conversationId;
            this.memberIds = memberIds;
        }
    }
}

package com.ping.android.domain.usecase.conversation;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.managers.UserManager;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/1/18.
 */

public class TogglePuzzlePictureUseCase extends UseCase<Boolean, TogglePuzzlePictureUseCase.Params> {
    @Inject
    CommonRepository commonRepository;
    UserManager userManager;

    @Inject
    public TogglePuzzlePictureUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        userManager = UserManager.getInstance();
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Params params) {
        String userId = userManager.getUser().key;
        Map<String, Object> updateValue = new HashMap<>();
        //updateValue.put(String.format("conversations/%s/puzzleMessages/%s", params.conversationId, userId), params.value);
        for(String userID: params.memberIds) {
            updateValue.put(String.format("conversations/%s/%s/puzzleMessages/%s", userID, params.conversationId, userId), params.value);
        }
        return commonRepository.updateBatchData(updateValue);
    }

    public static class Params {
        public String conversationId;
        public List<String> memberIds;
        public boolean value;

        public Params(String conversationId, List<String> memberIds, boolean value) {
            this.conversationId = conversationId;
            this.memberIds = memberIds;
            this.value = value;
        }
    }
}

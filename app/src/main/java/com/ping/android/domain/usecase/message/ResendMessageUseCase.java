package com.ping.android.domain.usecase.message;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.domain.repository.MessageRepository;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.ultility.Constant;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 3/5/18.
 */

public class ResendMessageUseCase extends UseCase<Boolean, ResendMessageUseCase.Params> {
    @Inject
    MessageRepository messageRepository;
    private Timer timer;

    @Inject
    public ResendMessageUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Params params) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                updateMessageStatus(params, Constant.MESSAGE_STATUS_ERROR)
                        .subscribe();
            }
        };
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(task, 5000);
        return updateMessageStatus(params, Constant.MESSAGE_STATUS_SENT)
                .flatMap(aBoolean -> {
                    if (timer != null) {
                        timer.cancel();
                    }
                    return updateMessageStatus(params, Constant.MESSAGE_STATUS_DELIVERED);
                });
    }

    private Observable<Boolean> updateMessageStatus(Params params, int messageStatus) {
        return messageRepository.updateMessageStatus(params.conversationId, params.message.key, params.currentUserId, messageStatus);
    }

    public static class Params {
        public String currentUserId;
        public String conversationId;
        public Message message;
    }
}

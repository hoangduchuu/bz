package com.ping.android.domain.usecase.message;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.bzzzchat.videorecorder.view.PhotoItem;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.model.enums.GameType;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/28/18.
 */

public class SendGroupGameMessageUseCase extends UseCase<Message, SendGroupGameMessageUseCase.Params> {
    @Inject
    SendGameMessageUseCase sendGameMessageUseCase;

    @Inject
    public SendGroupGameMessageUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Message> buildUseCaseObservable(Params params) {
        return Observable.just(params.items)
                .flatMapIterable(photoItems -> photoItems)
                .flatMap(photoItem ->  {
                    SendGameMessageUseCase.Params gameParams = new SendGameMessageUseCase.Params();
                    gameParams.conversation = params.conversation;
                    gameParams.currentUser = params.currentUser;
                    gameParams.filePath = photoItem.getImagePath();
                    gameParams.gameType = params.gameType;
                    gameParams.markStatus = params.markStatus;
                    return sendGameMessageUseCase.buildUseCaseObservable(gameParams);
                });
    }

    public static class Params {
        public List<PhotoItem> items;
        public Conversation conversation;
        public User currentUser;
        public GameType gameType;
        public boolean markStatus;
    }
}

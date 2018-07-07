package com.ping.android.domain.usecase.message;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.bzzzchat.videorecorder.view.PhotoItem;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.StorageRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.model.enums.MessageType;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/28/18.
 */

public class SendImagesMessageUseCase extends UseCase<List<Message>, SendImagesMessageUseCase.Params> {
    @Inject
    UserRepository userRepository;
    @Inject
    CommonRepository commonRepository;
    @Inject
    StorageRepository storageRepository;
    @Inject
    ConversationRepository conversationRepository;
    @Inject
    SendImageMessageUseCase sendImageMessageUseCase;

    @Inject
    public SendImagesMessageUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<List<Message>> buildUseCaseObservable(Params params) {
        PhotoItem[] items = new PhotoItem[params.items.size()];
        return Observable.fromArray(params.items.toArray(items))
                .concatMap(photoItem -> {
                    SendImageMessageUseCase.Params sendMessageParams = new SendImageMessageUseCase.Params();
                    sendMessageParams.filePath = photoItem.getImagePath();
                    sendMessageParams.thumbFilePath = photoItem.getThumbnailPath();
                    sendMessageParams.conversation = params.conversation;
                    sendMessageParams.currentUser = params.currentUser;
                    sendMessageParams.markStatus = params.markStatus;
                    sendMessageParams.messageType = MessageType.IMAGE;
                    return sendImageMessageUseCase.buildUseCaseObservable(sendMessageParams);
                })
                .toList()
                .toObservable();
    }

    public static class Params {
        public List<PhotoItem> items;
        public Conversation conversation;
        public User currentUser;
        public MessageType messageType;
        public boolean markStatus;
    }
}

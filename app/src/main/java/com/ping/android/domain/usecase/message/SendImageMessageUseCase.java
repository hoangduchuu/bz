package com.ping.android.domain.usecase.message;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.MessageRepository;
import com.ping.android.domain.repository.StorageRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.model.enums.GameType;
import com.ping.android.model.enums.MessageType;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/28/18.
 */

public class SendImageMessageUseCase extends UseCase<Message, SendImageMessageUseCase.Params> {
    @Inject
    UserRepository userRepository;
    @Inject
    CommonRepository commonRepository;
    @Inject
    StorageRepository storageRepository;
    @Inject
    ConversationRepository conversationRepository;
    @Inject
    SendMessageUseCase sendMessageUseCase;
    @Inject
    MessageRepository messageRepository;
    // builder;

    @Inject
    public SendImageMessageUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Message> buildUseCaseObservable(Params params) {
        SendMessageUseCase.Params.Builder builder = new SendMessageUseCase.Params.Builder()
                .setMessageType(params.messageType)
                .setConversation(params.conversation)
                .setMarkStatus(params.markStatus)
                .setCurrentUser(params.currentUser)
                .setGameType(params.gameType);
        builder.setCacheImage(params.filePath);
        builder.setFileUrl("PPhtotoMessageIdentifier");
        Message cachedMessage = builder.build().getMessage();
        cachedMessage.isCached = true;
        cachedMessage.localFilePath = params.filePath;
        return conversationRepository.getMessageKey(params.conversation.key)
                .zipWith(Observable.just(cachedMessage), (s, message) -> {
                    message.key = s;
                    builder.setMessageKey(s);
                    return message;
                })
                .flatMap(message -> sendMessageUseCase.buildUseCaseObservable(builder.build())
                        .map(message1 -> {
                            message1.isCached = true;
                            message1.localFilePath = params.filePath;
                            message1.currentUserId = params.currentUser.key;
                            return message1;
                        }))
                .flatMap(message -> sendMessage(params.conversation.key, message.key, params.filePath)
                        .map(aBoolean -> message));
    }

    private Observable<Boolean> sendMessage(String conversationId, String messageId, String filePath) {
        return this.uploadThumbnail(conversationId, messageId, filePath)
                .concatWith(uploadImage(conversationId, messageId, filePath))
                .map(s -> true);
    }

    private Observable<String> uploadImage(String conversationKey, String messageKey, String filePath) {
        if (TextUtils.isEmpty(filePath)) return Observable.just("");
        String fileName = new File(filePath).getName();
        return storageRepository.uploadFile(conversationKey, fileName, getImageData(filePath, 512, 512))
                .flatMap(s -> messageRepository.updateImage(conversationKey, messageKey, s));
    }

    private Observable<String> uploadThumbnail(String conversationKey, String messageKey, String filePath) {
        if (TextUtils.isEmpty(filePath)) return Observable.just("");
        String fileName = "thumb_" + new File(filePath).getName();
        return storageRepository.uploadFile(conversationKey, fileName, getImageData(filePath, 128, 128))
                .flatMap(s -> messageRepository.updateThumbnailImage(conversationKey, messageKey, s));
    }

    private static byte[] getImageData(String imagePath, int reqWidth, int reqHeight) {
        // Decode with inJustDecodeBounds = true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        byte[] byteArray = stream.toByteArray();
        bitmap.recycle();
        return byteArray;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keep boths
            // height and width larger then the requested height and width
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static class Params {
        public String filePath;
        public String thumbFilePath;
        public Conversation conversation;
        public User currentUser;
        public MessageType messageType;
        public GameType gameType;
        public boolean markStatus;
    }
}

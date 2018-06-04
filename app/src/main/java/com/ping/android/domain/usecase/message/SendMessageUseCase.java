package com.ping.android.domain.usecase.message;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.MessageRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.model.enums.GameType;
import com.ping.android.model.enums.MessageCallType;
import com.ping.android.model.enums.MessageType;
import com.ping.android.model.enums.VoiceType;
import com.ping.android.utils.CommonMethod;
import com.ping.android.utils.configs.Constant;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/8/18.
 */

public class SendMessageUseCase extends UseCase<Message, SendMessageUseCase.Params> {
    @Inject
    ConversationRepository conversationRepository;
    @Inject
    CommonRepository commonRepository;
    @Inject
    MessageRepository messageRepository;
    @Inject
    UserRepository userRepository;
    private Message message;
    private Conversation conversation;
    private Timer timer;

    @Inject
    public SendMessageUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        timer = new Timer();
    }

    @NotNull
    @Override
    public Observable<Message> buildUseCaseObservable(Params params) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                updateMessageStatus(Constant.MESSAGE_STATUS_ERROR)
                        .subscribe();
            }
        };
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(task, 5000);
        message = params.getMessage();
        message.days = (long) (message.timestamp * 1000 / Constant.MILLISECOND_PER_DAY);
        conversation = params.getConversation();
        return conversationRepository.sendMessage(params.conversation.key, message)
                .flatMap(message1 -> {
                    if (timer != null) {
                        timer.cancel();
                    }
                    Conversation conversation = params.getNewConversation();
                    Map<String, Object> updateData = new HashMap<>();
                    // Update message for conversation for each opponentUser
                    updateData.put(String.format("messages/%s/%s/status/%s", conversation.key,
                            message.key, message.senderId), Constant.MESSAGE_STATUS_DELIVERED);
                    for (String toUser : conversation.memberIDs.keySet()) {
                        if (!CommonMethod.getBooleanFrom(message1.readAllowed, toUser)) continue;
                        updateData.put(String.format("conversations/%s/%s", toUser, conversation.key), conversation.toMap());
                    }
                    return commonRepository.updateBatchData(updateData)
                            .map(success -> message1);
                });
    }

    private Observable<Boolean> updateMessageStatus(int messageStatus) {
        if (message != null) {
            //messageRepository.updateMessageStatus(conversation.key, message.key, message.senderId, messageStatus);
            HashMap<String, Object> updateValue = new HashMap<>();
            for (String userId : conversation.memberIDs.keySet()) {
                //messageRepository.updateMessageStatus(conversation.key, message.key, userId, messageStatus);
                updateValue.put(String.format("messages/%s/%s/status/%s", conversation.key, message.key, userId), messageStatus);
            }
            return commonRepository.updateBatchData(updateValue);
        }
        return Observable.empty();
    }

    public static class Params {
        private Conversation conversation;
        private Conversation newConversation;
        private Message message;
        private String filePath;

        public Message getMessage() {
            return message;
        }

        public Conversation getConversation() {
            return conversation;
        }

        public Conversation getNewConversation() {
            return newConversation;
        }

        public static class Builder {
            private Conversation conversation;
            private boolean markStatus;
            private User currentUser;
            private String text;
            private MessageType messageType;
            private GameType gameType;
            private VoiceType voiceType;
            private MessageCallType callType;
            private String fileUrl;
            private String thumbUrl;
            private String messageKey;
            private String cacheImage;
            private double timestamp;
            private double callDuration;

            public Builder() {
                timestamp = System.currentTimeMillis() / 1000d;
            }

            public Builder setCurrentUser(User currentUser) {
                this.currentUser = currentUser;
                return this;
            }

            public Builder setConversation(Conversation conversation) {
                this.conversation = conversation;
                return this;
            }

            public Builder setMarkStatus(boolean markStatus) {
                this.markStatus = markStatus;
                return this;
            }

            public Builder setMessageType(MessageType messageType) {
                this.messageType = messageType;
                return this;
            }

            public Builder setMessageKey(String messageKey) {
                this.messageKey = messageKey;
                return this;
            }

            // region text message
            public Builder setText(String text) {
                this.text = text;
                return this;
            }
            // endregion

            // region Game message
            public Builder setGameType(GameType gameType) {
                this.gameType = gameType;
                return this;
            }

            // endregion

            // region image message

            public Builder setFileUrl(String fileUrl) {
                this.fileUrl = fileUrl;
                this.text = fileUrl;
                return this;
            }

            public Builder setThumbUrl(String thumbUrl) {
                this.thumbUrl = thumbUrl;
                return this;
            }

            // endregion

            public Builder setVoiceType(VoiceType voiceType) {
                this.voiceType = voiceType;
                return this;
            }

            public Builder setCallType(MessageCallType callType) {
                this.callType = callType;
                return this;
            }

            public SendMessageUseCase.Params build() {
                Params params = new Params();
                Message message = null;
                switch (messageType) {
                    case TEXT:
                        message = buildMessage(currentUser, text);
                        break;
                    case IMAGE:
                        message = buildImageMessage(currentUser, fileUrl, thumbUrl);
                        break;
                    case GAME:
                        message = buildGameMessage(currentUser, fileUrl, gameType);
                        break;
                    case VOICE:
                        message = buildAudioMessage(currentUser, fileUrl, voiceType);
                        break;
                    case VIDEO:
                        message = buildVideoMessage(currentUser, fileUrl);
                        break;
                    case CALL:
                        message = buildCallMessage(currentUser, messageType, callType);
                        break;
                }
                message.key = messageKey;
                message.localFilePath = cacheImage;
                params.message = message;
                params.conversation = conversation;
                params.newConversation = conversationFrom(message);
                params.filePath = fileUrl;
                return params;
            }

            private Message buildCallMessage(User currentUser, MessageType messageType, MessageCallType callType) {
                this.currentUser = currentUser;
                Map<String, Boolean> allowance = getAllowance();
                return Message.createCallMessage(currentUser.key, currentUser.getDisplayName(), messageType,
                        timestamp, getStatuses(), getMessageMaskStatuses(), getMessageDeleteStatuses(), allowance, callType.ordinal(), callDuration);
            }

            private Message buildVideoMessage(User currentUser, String fileUrl) {
                Map<String, Boolean> allowance = getAllowance();
                return Message.createVideoMessage(fileUrl,
                        currentUser.key, currentUser.getDisplayName(), timestamp,
                        getStatuses(), getAudioMaskStatuses(voiceType),
                        getMessageDeleteStatuses(), allowance);
            }

            private Message buildAudioMessage(User currentUser, String audioUrl, VoiceType voiceType) {
                Map<String, Boolean> allowance = getAllowance();
                return Message.createAudioMessage(audioUrl,
                        currentUser.key, currentUser.getDisplayName(), timestamp, getStatuses(), getAudioMaskStatuses(voiceType),
                        getMessageDeleteStatuses(), allowance, voiceType.ordinal());
            }

            private Message buildMessage(User currentUser, String text) {
                this.currentUser = currentUser;
                Map<String, Boolean> allowance = getAllowance();
                return Message.createTextMessage(text, currentUser.key, currentUser.getDisplayName(),
                        timestamp, getStatuses(), getMessageMaskStatuses(), getMessageDeleteStatuses(), allowance);
            }

            private Message buildImageMessage(User currentUser, String photoUrl, String thumbUrl) {
                this.currentUser = currentUser;
                Map<String, Boolean> allowance = getAllowance();
                return Message.createImageMessage(photoUrl, thumbUrl, currentUser.key, currentUser.getDisplayName(),
                        timestamp, getStatuses(), getImageMaskStatuses(), getMessageDeleteStatuses(), allowance);
            }

            private Message buildGameMessage(User currentUser, String imageUrl, GameType gameType) {
                this.currentUser = currentUser;
                Map<String, Boolean> allowance = getAllowance();
                return Message.createGameMessage(imageUrl,
                        currentUser.key, currentUser.getDisplayName(), timestamp, getStatuses(), getImageMaskStatuses(),
                        getMessageDeleteStatuses(), allowance, gameType.ordinal());
            }

            private Conversation conversationFrom(Message message) {
                return new Conversation(conversation.conversationType, message.messageType, message.callType,
                        text, conversation.groupID, currentUser.key, getMemberIDs(), getMessageMaskStatuses(),
                        getMessageReadStatuses(), message.timestamp, conversation);
            }

            private Map<String, Boolean> getMemberIDs() {
                Map<String, Boolean> memberIDs = new HashMap<>();
                for (String toUser : conversation.memberIDs.keySet()) {
                    memberIDs.put(toUser, true);
                }
                return memberIDs;
            }

            private Map<String, Boolean> getMessageReadStatuses() {
                Map<String, Boolean> markStatuses = new HashMap<>();
                for (String toUser : conversation.memberIDs.keySet()) {
                    markStatuses.put(toUser, false);
                }
                markStatuses.put(currentUser.key, true);
                return markStatuses;
            }

            private Map<String, Boolean> getAllowance() {
                Map<String, Boolean> ret = new HashMap<>();
                ret.put(currentUser.key, true);
                for (String toUser : conversation.memberIDs.keySet()) {
                    if (toUser.equals(currentUser.key)) {
                        continue;
                    }
                    if (currentUser.blocks.containsKey(toUser)
                            || currentUser.blockBys.containsKey(toUser)) {
                        ret.put(toUser, false);
                    } else {
                        ret.put(toUser, true);
                    }
                }
                return ret;
            }

            private Map<String, Integer> getStatuses() {
                Map<String, Integer> deleteStatuses = new HashMap<>();
                for (String toUser : conversation.memberIDs.keySet()) {
                    deleteStatuses.put(toUser, Constant.MESSAGE_STATUS_SENT);
                }
                deleteStatuses.put(currentUser.key, Constant.MESSAGE_STATUS_SENT);
                return deleteStatuses;
            }

            private Map<String, Boolean> getAudioMaskStatuses(VoiceType voiceType) {
                Map<String, Boolean> maskStatuses = new HashMap<>();
                if (voiceType == VoiceType.DEFAULT) {
                    return maskStatuses;
                }
                for (String userId : conversation.memberIDs.keySet()) {
                    maskStatuses.put(userId, true);
                }
                return maskStatuses;
            }

            private Map<String, Boolean> getMessageMaskStatuses() {
                Map<String, Boolean> markStatuses = new HashMap<>();
                if (conversation.maskMessages != null) {
                    markStatuses.putAll(conversation.maskMessages);
                }
                markStatuses.put(currentUser.key, markStatus);
                return markStatuses;
            }

            private Map<String, Boolean> getImageMaskStatuses() {
                Map<String, Boolean> maskStatuses = new HashMap<>();
                if (conversation.puzzleMessages != null) {
                    maskStatuses.putAll(conversation.puzzleMessages);
                }
                maskStatuses.put(currentUser.key, markStatus);
                return maskStatuses;
            }

            private Map<String, Boolean> getMessageDeleteStatuses() {
                Map<String, Boolean> deleteStatuses = new HashMap<>();
                for (String toUser : conversation.memberIDs.keySet()) {
                    deleteStatuses.put(toUser, false);
                }
                deleteStatuses.put(currentUser.key, false);
                return deleteStatuses;
            }

            public Builder setCacheImage(String filePath) {
                this.cacheImage = filePath;
                return this;
            }

            public Builder setCallDuration(double callDuration) {
                this.callDuration = callDuration;
                return this;
            }
        }

        public void setConversation(Conversation conversation) {
            this.conversation = conversation;
        }

        public void setMessageKey(String key) {
            message.key = key;
        }

        public String getFilePath() {
            return filePath;
        }
    }
}

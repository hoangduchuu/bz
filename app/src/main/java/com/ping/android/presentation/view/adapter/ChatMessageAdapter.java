package com.ping.android.presentation.view.adapter;

import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.bzzzchat.flexibleadapter.FlexibleAdapter;
import com.bzzzchat.flexibleadapter.FlexibleItem;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.model.enums.MessageType;
import com.ping.android.presentation.view.custom.revealable.RevealableViewRecyclerView;
import com.ping.android.presentation.view.custom.revealable.RevealableViewHolder;
import com.ping.android.presentation.view.flexibleitem.messages.AudioMessageBaseItem;
import com.ping.android.presentation.view.flexibleitem.messages.MessageBaseItem;
import com.ping.android.presentation.view.flexibleitem.messages.MessageHeaderItem;
import com.ping.android.presentation.view.flexibleitem.messages.TypingItem;
import com.ping.android.presentation.view.flexibleitem.messages.GroupImageMessageBaseItem;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by tuanluong on 3/2/18.
 */

public class ChatMessageAdapter extends FlexibleAdapter<FlexibleItem> implements
        MessageBaseItem.MessageListener, RevealableViewRecyclerView.RevealableCallback {
    public static float xDiff = 0;
    private ChatMessageListener messageListener;
    private List<MessageBaseItem> selectedMessages;
    private TypingItem typingItem;

    private Map<String, String> nickNames = new HashMap<>();

    private Set<RecyclerView.ViewHolder> boundsViewHolder = new HashSet<>();
    private boolean isEditMode = false;
    private RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();

    public static MediaPlayer audioPlayerInstance = null;
    public static AudioMessageBaseItem currentPlayingMessage = null;

    public ChatMessageAdapter() {
        super();
        if (audioPlayerInstance == null) {
            audioPlayerInstance = new MediaPlayer();
            audioPlayerInstance.setOnCompletionListener(mediaPlayer -> {
                if (currentPlayingMessage != null) {
                    currentPlayingMessage.completePlaying();
                    onCompletePlayAudio(currentPlayingMessage);
                }
            });
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        boundsViewHolder.remove(holder);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = super.onCreateViewHolder(parent, viewType);
        if (holder instanceof GroupImageMessageBaseItem.ViewHolder) {
            ((GroupImageMessageBaseItem.ViewHolder)holder).setRecycledViewPool(viewPool);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        boundsViewHolder.add(holder);
        if (holder instanceof MessageBaseItem.ViewHolder) {
            ((MessageBaseItem.ViewHolder) holder).setMessageListener(this);
            ((MessageBaseItem.ViewHolder) holder).setNickNames(nickNames);
        }
        super.onBindViewHolder(holder, position);
    }

    public void setMessageListener(ChatMessageListener messageListener) {
        this.messageListener = messageListener;
        selectedMessages = new ArrayList<>();
    }

    @Override
    public void onProfileImagePress(User user, Pair<View, String>[] sharedElements) {
        if (messageListener != null) {
            messageListener.handleProfileImagePress(user, sharedElements);
        }
    }

    @Override
    public void updateMessageMask(Message message, boolean maskStatus, boolean lastItem) {
        if (messageListener != null) {
            messageListener.updateMessageMask(message, maskStatus, lastItem);
        }
    }

    @Override
    public void onLongPress(MessageBaseItem messageItem) {
        if (messageListener != null) {
            messageListener.onLongPress(messageItem, messageItem.message.type == MessageType.TEXT);
        }
        selectedMessages.add(messageItem);
    }

    @Override
    public void openImage(String messageKey, String imageUrl, String localImage, boolean isPuzzled, Pair<View, String>[] sharedElements) {
        if (messageListener != null) {
            messageListener.openImage(messageKey, imageUrl, localImage, isPuzzled, sharedElements);
        }
    }

    @Override
    public void openGameMessage(Message message) {
        if (messageListener != null) {
            messageListener.openGameMessage(message);
        }
    }

    @Override
    public void onPauseAudioMessage(AudioMessageBaseItem message) {
        int index = this.items.indexOf(message);
        if (index >= 0 && index < getItemCount()) {
            notifyItemChanged(index);
        }
    }

    @Override
    public void onCompletePlayAudio(AudioMessageBaseItem audioMessageBaseItem) {
        int index = this.items.indexOf(audioMessageBaseItem);
        if (index >= 0 && index < getItemCount()) {
            notifyItemChanged(index);
        }
    }

    @Override
    public void selectMessage(MessageBaseItem item) {
        selectedMessages.add(item);
        if (messageListener != null) {
            messageListener.updateMessageSelection(selectedMessages.size());
        }
    }

    @Override
    public void unSelectMessage(MessageBaseItem item) {
        selectedMessages.remove(item);
        if (messageListener != null) {
            messageListener.updateMessageSelection(selectedMessages.size());
        }
    }

    @Override
    public void openVideo(@NotNull String videoUrl) {
        if (messageListener != null) {
            messageListener.openVideo(videoUrl);
        }
    }

    @Override
    public void onCall(boolean isVideo) {
        if (messageListener != null) {
            messageListener.onCall(isVideo);
        }
    }

    @Override
    public void onGroupImageItemPress(GroupImageMessageBaseItem.ViewHolder viewHolder, @NotNull List<Message> data, int position, Pair<View, String>... sharedElements) {
        if (messageListener != null) {
            messageListener.onGroupImageItemPress(viewHolder, data, position, sharedElements);
        }
    }

    @Override
    public void updateChildMessageMask(Message message, boolean maskStatus) {
        if (messageListener != null) {
            messageListener.updateChildMessageMask(message, maskStatus);
        }
    }

    public void pause() {
        if (currentPlayingMessage != null) {
            currentPlayingMessage.stopSelf();
            onPauseAudioMessage(currentPlayingMessage);
        }
    }

    public void destroy() {
        if (audioPlayerInstance != null) {
            audioPlayerInstance.stop();
            audioPlayerInstance.release();
            audioPlayerInstance = null;
        }
    }

    public List<Message> getSelectedMessages() {
        List<Message> messages = new ArrayList<>(selectedMessages.size());
        for (MessageBaseItem item : selectedMessages) {
            messages.add(item.message);
        }
        return messages;
    }

    public void showTyping() {
        if (typingItem == null) {
            typingItem = new TypingItem();
        }
        int index = items.indexOf(typingItem);
        if (index >= 0 && index < items.size()) {
            if (index == items.size() - 1) {
                return;
            } else {
                remove(index);
            }
        }
        add(typingItem);
    }

    private void remove(int index) {
        items.remove(index);
        notifyItemRemoved(index);
    }

    public void hideTypingItem() {
        if (typingItem == null) return;
        int index = this.items.indexOf(typingItem);
        if (index >= 0 && index < getItemCount()) {
            this.items.remove(index);
            notifyItemRemoved(index);
        }
    }

    public Message getLastMessage() {
        for (int i = getItemCount() - 1; i >= 0; i--) {
            if (this.items.get(i) instanceof MessageBaseItem) {
                return ((MessageBaseItem) this.items.get(i)).message;
            }
        }
        return null;
    }

    public void update(MessageBaseItem selectedMessage) {
        int index = this.items.indexOf(selectedMessage);
        if (index >= 0 && index < getItemCount()) {
            this.items.set(index, selectedMessage);
            notifyItemChanged(index);
        }
    }

    public FlexibleItem getItem(int i) {
        return this.items.get(i);
    }

    public void updateNickNames(Map<String, String> nickNames) {
        this.nickNames = nickNames;
        notifyDataSetChanged();
    }

    public void deleteMessage(MessageHeaderItem headerItem, MessageBaseItem item) {
        boolean shouldUpdateLastConversationMessage = false;
        Message message = getLastMessage();
        if (message != null && message.key.equals(item.message.key)) {
            shouldUpdateLastConversationMessage = true;
        }

        int headerIndex = items.indexOf(headerItem);
        if (headerIndex >= 0) {
            int childIndex = headerItem.removeMessage(item);
            if (headerIndex + childIndex < items.size()) {
                // Must plus 1 because childIndex is start from 0
                int finalIndex = headerIndex + childIndex + 1;
                //item.setMessageListener(null);
                items.remove(finalIndex);
                notifyItemRemoved(finalIndex);
            }
            if (headerItem.getChildItems().size() == 0) {
                items.remove(headerIndex);
                notifyItemRemoved(headerIndex);
            }
        }
        if (shouldUpdateLastConversationMessage) {
            Message lastMessage = getLastMessage();
            if (messageListener != null) {
                messageListener.updateLastConversationMessage(lastMessage);
            }
        }
    }

    public void updateEditMode(boolean isEditMode) {
        if (!isEditMode) {
            selectedMessages.clear();
        }
        this.isEditMode = isEditMode;
        for (FlexibleItem item : this.items) {
            if (item instanceof MessageBaseItem) {
                ((MessageBaseItem) item).setEditMode(isEditMode);
                if (!isEditMode) {
                    ((MessageBaseItem) item).setSelected(false);
                }
            }
        }
        notifyDataSetChanged();
    }


    public void updateData(List<MessageHeaderItem> headerItems) {
        //this.items.clear();
        for (int i = headerItems.size() - 1; i >= 0; i--) {
            MessageHeaderItem headerItem = headerItems.get(i);
            int newItemsSize = headerItem.getNewItems().size();
            if (newItemsSize > 0) {
                // Find header index
                int index = this.items.indexOf(headerItem);
                if (index < 0) {
                    index = 0;
                    // Add header to list
                    this.items.add(index, headerItem);
                    notifyItemInserted(index);
                }
                List<MessageBaseItem> items = headerItem.getNewItems();

                this.items.addAll(index + 1, headerItem.getNewItems());
                headerItem.processNewItems();
                notifyItemRangeInserted(index + 1, newItemsSize);
            }
        }
    }

    public void handleNewMessage(MessageBaseItem item, MessageHeaderItem headerItem, boolean added) {
        int headerIndex = this.items.indexOf(headerItem);
        if (headerIndex < 0) {
            int size = this.items.size();
            this.items.add(headerItem);
            headerIndex = size;
            notifyItemInserted(size);
        }
        if (added) {
            int childIndex = headerItem.findChildIndex(item);
            int finalIndex = headerIndex + childIndex + 1;
            this.items.add(finalIndex, item);
            notifyItemInserted(finalIndex);
            if (finalIndex == getItemCount() - 1 && finalIndex > 0) {
                notifyItemChanged(finalIndex - 1);
            }
        } else {
            int index = this.items.indexOf(item);
            if (index > 0) {
                this.items.set(index, item);
                notifyItemChanged(index);
            }
        }
    }

    @Override
    public void onDragged(float xDiff) {
        if (isEditMode) return;
        for (RecyclerView.ViewHolder viewHolder : boundsViewHolder) {
            if (viewHolder instanceof RevealableViewHolder) {
                ((RevealableViewHolder) viewHolder).transform(xDiff);
            }
        }
    }

    @Override
    public void onReset() {
        for (RecyclerView.ViewHolder viewHolder : boundsViewHolder) {
            if (viewHolder instanceof RevealableViewHolder) {
                ((RevealableViewHolder) viewHolder).transform(0);
            }
        }
    }

    public List<Message> findMessages(int firstVisible, int lastVisible) {
        List<Message> messageBaseItems = new ArrayList<>();
        if (firstVisible >= 0 && firstVisible < getItemCount()
                && lastVisible >= 0 && lastVisible < getItemCount()) {
            for (int i = firstVisible; i <= lastVisible; i++) {
                FlexibleItem item = items.get(i);
                if (item instanceof MessageBaseItem) {
                    messageBaseItems.add(((MessageBaseItem) item).message);
                }
            }
        }
        return messageBaseItems;
    }

    public interface ChatMessageListener {
        void handleProfileImagePress(User user, Pair<View, String>... sharedElements);

        void updateMessageMask(Message message, boolean markStatus, boolean lastItem);

        void onLongPress(MessageBaseItem message, boolean allowCopy);

        void openImage(String messageKey, String imageUrl, String localImage, boolean isPuzzled, Pair<View, String>... sharedElements);

        void openGameMessage(Message message);

        void updateMessageSelection(int size);

        void updateLastConversationMessage(Message lastMessage);

        void openVideo(String videoUrl);

        void onCall(boolean isVideo);

        void onGroupImageItemPress(GroupImageMessageBaseItem.ViewHolder viewHolder, @NotNull List<Message> data, int position, Pair<View, String>... sharedElements);

        void updateChildMessageMask(Message message, boolean maskStatus);
    }
}

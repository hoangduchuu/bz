package com.ping.android.presentation.view.adapter;

import android.media.MediaPlayer;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.RequestManager;
import com.bzzzchat.flexibleadapter.FlexibleAdapter;
import com.bzzzchat.flexibleadapter.FlexibleItem;
import com.ping.android.model.Message;
import com.ping.android.model.enums.MessageType;
import com.ping.android.presentation.view.custom.revealable.RevealableViewHolder;
import com.ping.android.presentation.view.custom.revealable.RevealableViewRecyclerView;
import com.ping.android.presentation.view.flexibleitem.messages.AudioMessageBaseItem;
import com.ping.android.presentation.view.flexibleitem.messages.GroupImageMessageBaseItem;
import com.ping.android.presentation.view.flexibleitem.messages.MessageBaseItem;
import com.ping.android.presentation.view.flexibleitem.messages.MessageHeaderItem;
import com.ping.android.presentation.view.flexibleitem.messages.TypingItem;

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
    private final RequestManager glide;
    private ChatMessageListener messageListener;
    private List<MessageBaseItem> selectedMessages;
    private TypingItem typingItem;
    private Boolean faceIdRecognitionStatus;
    private Map<String, String> nickNames = new HashMap<>();

    private Set<RecyclerView.ViewHolder> boundsViewHolder = new HashSet<>();
    private boolean isEditMode = false;
    private RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();

    public static MediaPlayer audioPlayerInstance = null;
    public static AudioMessageBaseItem currentPlayingMessage = null;

    private Object lock = new Object();

    public ChatMessageAdapter(RequestManager glide) {
        super();
        this.glide = glide;
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
        if (holder instanceof MessageBaseItem.ViewHolder) {
            ((MessageBaseItem.ViewHolder) holder).setGlide(glide);
        }
        if (holder instanceof GroupImageMessageBaseItem.ViewHolder) {
            ((GroupImageMessageBaseItem.ViewHolder) holder).setRecycledViewPool(viewPool);
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
    public void onProfileImagePress(String senderId, Pair<View, String>[] sharedElements) {
        if (messageListener != null) {
            messageListener.handleProfileImagePress(senderId, sharedElements);
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
    public void openImage(Message message, boolean isPuzzled, Pair<View, String>[] sharedElements) {
        if (messageListener != null) {
            messageListener.openImage(message, isPuzzled, sharedElements);
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
        synchronized (lock) {
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
    }

    private void remove(int index) {
        items.remove(index);
        notifyItemRemoved(index);
    }

    public void hideTypingItem() {
        if (typingItem == null) return;
        synchronized (lock) {
            int index = this.items.indexOf(typingItem);
            if (index >= 0 && index < getItemCount()) {
                this.items.remove(index);
                notifyItemRemoved(index);
            }
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

//    public Message getLastMessage() {
//        if (getItemCount() > 0) {
//            return ((MessageBaseItem) this.items.get(0)).message;
//        }
//        return null;
//    }

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
        List<FlexibleItem> result = new ArrayList<>();
        for (int i = headerItems.size() - 1; i >= 0; i--) {
            MessageHeaderItem headerItem = headerItems.get(i);
            List<MessageBaseItem> newItems = headerItem.getNewItems();
            int newItemsSize = newItems.size();
            if (newItemsSize > 0) {
                // Find header index
                result.add(0, headerItem);
//                int index = this.items.indexOf(headerItem);
//                if (index < 0) {
//                    index = 0;
//                    // Add header to list
//                    this.items.add(index, headerItem);
//                    //notifyItemInserted(index);
//                }

                result.addAll(1, newItems);
                headerItem.processNewItems();
                //notifyItemRangeInserted(index + 1, newItemsSize);
            }
        }
//        if (items.size() == 0) {
        items.clear();
        items.addAll(result);
        notifyDataSetChanged();
//            return;
//        }
//        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new MessageDiffCallback(this.items, result));
//        diffResult.dispatchUpdatesTo(this);
//        this.items.clear();
//        this.items.addAll(result);
    }

    public void appendMessages(List<MessageHeaderItem> headerItems) {
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

                this.items.addAll(index + 1, headerItem.getNewItems());
                headerItem.processNewItems();
                notifyItemRangeInserted(index + 1, newItemsSize);
            }
        }
    }

    public void handleNewMessage(MessageBaseItem item, MessageHeaderItem headerItem, MessageHeaderItem higherHeaderItem, boolean added) {
        synchronized (lock) {
            int headerIndex = this.items.indexOf(headerItem);
            if (headerIndex < 0) {
                if (higherHeaderItem != null) {
                    headerIndex = this.items.indexOf(higherHeaderItem);
                }
                if (headerIndex < 0) {
                    headerIndex = this.items.size();
                }
                this.items.add(headerIndex, headerItem);
                notifyItemInserted(headerIndex);
                if (headerIndex > 0) {
                    // Refresh last item in previous section
                    notifyItemChanged(headerIndex - 1);
                }
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

    public void userRecognized(Boolean value) {
        faceIdRecognitionStatus = value;
        for (FlexibleItem item: this.items) {
            if (item instanceof MessageBaseItem) {
                ((MessageBaseItem) item).message.faceIdRecognitionStatus = value;
            }
        }
        notifyDataSetChanged();
    }

    public interface ChatMessageListener {
        void handleProfileImagePress(String senderId, Pair<View, String>... sharedElements);

        void updateMessageMask(Message message, boolean markStatus, boolean lastItem);

        void onLongPress(MessageBaseItem message, boolean allowCopy);

        void openImage(Message message, boolean isPuzzled, Pair<View, String>... sharedElements);

        void openGameMessage(Message message);

        void updateMessageSelection(int size);

        void updateLastConversationMessage(Message lastMessage);

        void openVideo(String videoUrl);

        void onCall(boolean isVideo);

        void onGroupImageItemPress(GroupImageMessageBaseItem.ViewHolder viewHolder, @NotNull List<Message> data, int position, Pair<View, String>... sharedElements);

        void updateChildMessageMask(Message message, boolean maskStatus);
    }

    public class MessageDiffCallback extends DiffUtil.Callback {
        private List<FlexibleItem> oldItems;
        private List<FlexibleItem> newItems;

        public MessageDiffCallback(List<FlexibleItem> oldItems, List<FlexibleItem> newItems) {
            this.oldItems = oldItems;
            this.newItems = newItems;
        }

        @Override
        public int getOldListSize() {
            return oldItems.size();
        }

        @Override
        public int getNewListSize() {
            return newItems.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            FlexibleItem oldItem = oldItems.get(oldItemPosition);
            FlexibleItem newItem = newItems.get(oldItemPosition);
//            if (oldItem instanceof MessageHeaderItem) {
//                return oldItem.equals(newItem);
//            }
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return true;
        }
    }
}

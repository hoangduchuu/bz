package com.ping.android.presentation.view.adapter;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.bzzzchat.flexibleadapter.FlexibleAdapter;
import com.bzzzchat.flexibleadapter.FlexibleItem;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.presentation.view.custom.revealable.RevealableViewContainer;
import com.ping.android.presentation.view.custom.revealable.RevealableViewHolder;
import com.ping.android.presentation.view.flexibleitem.messages.AudioMessageBaseItem;
import com.ping.android.presentation.view.flexibleitem.messages.MessageBaseItem;
import com.ping.android.presentation.view.flexibleitem.messages.MessageHeaderItem;
import com.ping.android.presentation.view.flexibleitem.messages.PaddingItem;
import com.ping.android.presentation.view.flexibleitem.messages.TypingItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by tuanluong on 3/2/18.
 */

public class ChatMessageAdapter extends FlexibleAdapter<FlexibleItem> implements
        MessageBaseItem.MessageListener, RevealableViewContainer.RevealableCallback {
    public static float xDiff = 0;
    private ChatMessageListener messageListener;
    private List<MessageBaseItem> selectedMessages;
    private TypingItem typingItem;
    private PaddingItem paddingItem;

    private Set<RecyclerView.ViewHolder> boundsViewHolder = new HashSet<>();

    public ChatMessageAdapter() {
        super();
        addPadding();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        boundsViewHolder.remove(holder);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        boundsViewHolder.add(holder);
        super.onBindViewHolder(holder, position);
    }

    public void setMessageListener(ChatMessageListener messageListener) {
        this.messageListener = messageListener;
        selectedMessages = new ArrayList<>();
    }

    public void addOrUpdate(MessageBaseItem messageItem) {
        boolean isAdd = true;
        int size = getItemCount();
        int index = getItemCount();
        for (int i = size - 1; i >= 0; i--) {
            FlexibleItem item = items.get(i);
            if (!(item instanceof MessageBaseItem)) {
                continue;
            }
            if (messageItem.message.key.equals(((MessageBaseItem) item).message.key)) {
                index = i;
                if (!TextUtils.isEmpty(((MessageBaseItem) item).message.localImage)) {
                    // Keep local image

                    messageItem.message.localImage = ((MessageBaseItem) item).message.localImage;
                }
                ((MessageBaseItem) item).message = messageItem.message;
                update(item, index);
                isAdd = false;
                break;
            }
            if (messageItem.message.timestamp > ((MessageBaseItem) item).message.timestamp) {
                index = i + 1;
                break;
            }
        }

        if (isAdd) {
            messageItem.setMessageListener(this);
            add(messageItem, index);
            // If new message has come, add to list then update previous message to hide its status
            if (index == getItemCount() - 1 && index > 0) {
                notifyItemChanged(index - 1);
            }
        }
    }

    public void deleteMessage(String key) {
        MessageBaseItem deleteItem = null;
        for (int i = getItemCount() - 1; i >= 0; i--) {
            FlexibleItem item = this.items.get(i);
            if (item instanceof MessageBaseItem) {
                Message message = ((MessageBaseItem) item).message;
                if (key.equals(message.key)) {
                    deleteItem = (MessageBaseItem) item;
                    break;
                }
            }
        }
        if (deleteItem != null) {
            boolean shouldUpdateLastConversationMessage = false;
            Message message = getLastMessage();
            if (message != null && message.key.equals(key)) {
                shouldUpdateLastConversationMessage = true;
            }
            deleteItem.setMessageListener(null);
            int index = this.items.indexOf(deleteItem);
            this.items.remove(index);
            notifyItemRemoved(index);

            if (shouldUpdateLastConversationMessage) {
                Message lastMessage = getLastMessage();
                if (messageListener != null && lastMessage != null) {
                    messageListener.updateLastConversationMessage(lastMessage);
                }
            }
        }
    }

    public void updateEditMode(boolean isEditMode) {
        if (!isEditMode) {
            selectedMessages.clear();
        }
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

    @Override
    public void handleProfileImagePress(User user, Pair<View, String>[] sharedElements) {
        if (messageListener != null) {
            messageListener.handleProfileImagePress(user, sharedElements);
        }
    }

    @Override
    public void updateMessageMask(Message message, boolean markStatus, boolean lastItem) {
        if (messageListener != null) {
            messageListener.updateMessageMask(message, markStatus, lastItem);
        }
    }

    @Override
    public void onLongPress(MessageBaseItem messageItem) {
        if (messageListener != null) {
            messageListener.onLongPress(messageItem);
        }
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

    public void pause() {
        if (AudioMessageBaseItem.currentPlayingMessage != null) {
            AudioMessageBaseItem.currentPlayingMessage.stopSelf();
            onPauseAudioMessage(AudioMessageBaseItem.currentPlayingMessage);
        }
    }

    public void destroy() {
        if (AudioMessageBaseItem.currentPlayingMessage != null) {
            AudioMessageBaseItem.currentPlayingMessage.release();
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

    public void resetSelectedMessages() {
        selectedMessages.clear();
    }

    public void update(MessageBaseItem selectedMessage) {
        int index = this.items.indexOf(selectedMessage);
        if (index >= 0 && index < getItemCount()) {
            this.items.set(index, selectedMessage);
            notifyItemChanged(index);
        }
    }

    public void updateNickNames(Map<String, String> nickNames) {
        for (FlexibleItem item : this.items) {
            if (item instanceof MessageBaseItem) {
                ((MessageBaseItem) item).setNickNames(nickNames);
            }
        }
        notifyDataSetChanged();
    }

    public void appendHistoryItems(List<MessageBaseItem> messages) {
        for (MessageBaseItem item : messages) {
            item.setMessageListener(this);
        }
        int startIndex = this.items.size() >= 1 ? 1 : 0;
        int endIndex = messages.size();
        this.items.addAll(startIndex, messages);
        notifyItemRangeInserted(startIndex, endIndex);
    }

    private void addPadding() {
        if (paddingItem == null) {
            paddingItem = new PaddingItem();
        }
        add(paddingItem);
    }

    public FlexibleItem getItem(int i) {
        return this.items.get(i);
    }

    public void updateData(List<MessageHeaderItem> messages) {
        this.items.clear();
        for (MessageHeaderItem headerItem : messages) {
            this.items.add(headerItem);
            this.items.addAll(headerItem.getChildItems());
        }
        notifyDataSetChanged();
    }

    public void handleNewMessage(MessageBaseItem item, MessageHeaderItem headerItem, boolean added) {
        int headerIndex = this.items.indexOf(headerItem);
        if (headerIndex < 0) {
            int size = this.items.size();
            this.items.add(headerItem);
            headerIndex = size;
            notifyItemInserted(size);
        }
        item.setMessageListener(this);
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

    public interface ChatMessageListener {
        void handleProfileImagePress(User user, Pair<View, String>... sharedElements);

        void updateMessageMask(Message message, boolean markStatus, boolean lastItem);

        void onLongPress(MessageBaseItem message);

        void openImage(String messageKey, String imageUrl, String localImage, boolean isPuzzled, Pair<View, String>... sharedElements);

        void openGameMessage(Message message);

        void updateMessageSelection(int size);

        void updateLastConversationMessage(Message lastMessage);
    }
}

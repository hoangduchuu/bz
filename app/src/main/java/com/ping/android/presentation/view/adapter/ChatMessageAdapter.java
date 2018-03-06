package com.ping.android.presentation.view.adapter;

import android.text.TextUtils;

import com.bzzzchat.flexibleadapter.FlexibleAdapter;
import com.bzzzchat.flexibleadapter.FlexibleItem;
import com.ping.android.model.Message;
import com.ping.android.presentation.view.flexibleitem.messages.MessageBaseItem;

/**
 * Created by tuanluong on 3/2/18.
 */

public class ChatMessageAdapter extends FlexibleAdapter<FlexibleItem> {
    private MessageBaseItem.MessageListener messageListener;

    public void setMessageListener(MessageBaseItem.MessageListener messageListener) {
        this.messageListener = messageListener;
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
                isAdd = false;
                break;
            }
            if (messageItem.message.timestamp > ((MessageBaseItem) item).message.timestamp) {
                index = i + 1;
                break;
            }
        }

        if (isAdd) {
            messageItem.setMessageListener(messageListener);
            add(messageItem, index);
            // If new message has come, add to list then update previous message to hide its status
            if (index == getItemCount() - 1 && index > 0) {
                notifyItemChanged(index - 1);
            }
        } else {
            update(messageItem, index);
        }
    }

    public void deleteMessage(String key) {
        FlexibleItem deleteItem = null;
        for (FlexibleItem item : this.items) {
            if (item instanceof MessageBaseItem) {
                Message message = ((MessageBaseItem) item).message;
                if (key.equals(message.key)) {
                    deleteItem = item;
                    break;
                }
            }
        }
        if (deleteItem != null) {
            ((MessageBaseItem)deleteItem).setMessageListener(null);
            int index = this.items.indexOf(deleteItem);
            this.items.remove(index);
            notifyItemRemoved(index);
        }
    }

    public void updateEditMode(boolean isEditMode) {
        for (FlexibleItem item : this.items) {
            if (item instanceof MessageBaseItem) {
                ((MessageBaseItem) item).setEditMode(isEditMode);
            }
        }
        notifyDataSetChanged();
    }
}

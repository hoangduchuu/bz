package com.ping.android.presentation.view.flexibleitem.messages;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ping.android.activity.R;
import com.ping.android.model.Message;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.CommonMethod;

import org.jetbrains.annotations.NotNull;

/**
 * Created by tuanluong on 3/2/18.
 */

public abstract class TextMessageBaseItem extends MessageBaseItem<TextMessageBaseItem.ViewHolder> {

    public TextMessageBaseItem(Message message) {
        super(message);
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(getLayoutId(), parent, false);
        return new ViewHolder(view);
    }

    public static class ViewHolder extends MessageBaseItem.ViewHolder {
        LinearLayout messageContainer;
        TextView txtMessage;

        public ViewHolder(View itemView) {
            super(itemView);
            messageContainer = itemView.findViewById(R.id.item_chat_message);
            txtMessage = itemView.findViewById(R.id.item_chat_text);

            initGestureListener();
        }

        @Override
        public void bindData(MessageBaseItem item, boolean lastItem) {
            super.bindData(item, lastItem);
            this.setTextMessage(item.message);
        }

        @Override
        protected View getClickableView() {
            return messageContainer;
        }

        @Override
        public void onDoubleTap() {
            this.maskStatus = !this.maskStatus;
//            String text = item.message.message;
//            if (maskStatus) {
//                text = ServiceManager.getInstance().encodeMessage(itemView.getContext(), text);
//            }
//            txtMessage.setText(text);
            if (messageListener != null) {
                messageListener.updateMessageMask(item.message, maskStatus, lastItem);
            }
        }

        private void setTextMessage(Message message) {
            String messageText = message.message;
            boolean shouldMaskMessage = CommonMethod.getBooleanFrom(message.markStatuses, message.currentUserId);
            if (shouldMaskMessage) {
                messageText = ServiceManager.getInstance().encodeMessage(itemView.getContext(), message.message);
            }
            txtMessage.setText(messageText);
        }
    }
}

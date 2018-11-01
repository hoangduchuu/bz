package com.ping.android.presentation.view.flexibleitem.messages;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ping.android.App;
import com.ping.android.R;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Message;

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
        UserManager userManager;

        public ViewHolder(View itemView) {
            super(itemView);
            userManager = ((App)itemView.getContext().getApplicationContext()).getComponent().provideUserManager();
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
            if (messageListener != null) {
                messageListener.updateMessageMask(item.message, maskStatus, lastItem);
            }
        }

        @Override
        public View getSlideView() {
            return messageContainer;
        }

        private void setTextMessage(Message message) {
            String messageText = message.message;
            if (message.isMask) {
                messageText = userManager.encodeMessage(message.message);
            }
            txtMessage.setText(messageText);
        }
    }
}

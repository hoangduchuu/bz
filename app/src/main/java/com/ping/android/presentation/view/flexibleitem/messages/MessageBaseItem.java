package com.ping.android.presentation.view.flexibleitem.messages;

import android.support.transition.TransitionManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bzzzchat.flexibleadapter.FlexibleItem;
import com.ping.android.R;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.presentation.view.adapter.ChatMessageAdapter;
import com.ping.android.presentation.view.custom.revealable.RevealStyle;
import com.ping.android.presentation.view.custom.revealable.RevealableViewHolder;
import com.ping.android.presentation.view.flexibleitem.messages.audio.AudioMessageLeftItem;
import com.ping.android.presentation.view.flexibleitem.messages.audio.AudioMessageRightItem;
import com.ping.android.presentation.view.flexibleitem.messages.image.ImageMessageLeftItem;
import com.ping.android.presentation.view.flexibleitem.messages.image.ImageMessageRightItem;
import com.ping.android.presentation.view.flexibleitem.messages.text.TextMessageLeftItem;
import com.ping.android.presentation.view.flexibleitem.messages.text.TextMessageRightItem;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.DateUtils;
import com.ping.android.utils.ResourceUtils;
import com.ping.android.utils.UiUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tuanluong on 3/2/18.
 */

public abstract class MessageBaseItem<VH extends MessageBaseItem.ViewHolder> implements FlexibleItem<VH> {
    //private Map<String, String> nickNames = new HashMap<>();
    public Message message;
    public int conversationType;
    protected boolean isEditMode = false;
    protected boolean isSelected = false;

    public static MessageBaseItem from(Message message, String currentUserID, int conversationType) {
        MessageBaseItem baseItem;
        switch (message.messageType) {
            case Constant.MSG_TYPE_IMAGE:
            case Constant.MSG_TYPE_GAME:
                if (message.senderId.equals(currentUserID)) {
                    baseItem = new ImageMessageRightItem(message);
                } else {
                    baseItem = new ImageMessageLeftItem(message);
                }
                break;
            case Constant.MSG_TYPE_VOICE:
                if (message.senderId.equals(currentUserID)) {
                    baseItem = new AudioMessageRightItem(message);
                } else {
                    baseItem = new AudioMessageLeftItem(message);
                }
                break;
            default:
                if (message.senderId.equals(currentUserID)) {
                    baseItem = new TextMessageRightItem(message);
                } else {
                    baseItem = new TextMessageLeftItem(message);
                }
                break;
        }
        baseItem.conversationType = conversationType;
        return baseItem;
    }

    public MessageBaseItem(Message message) {
        this.message = message;
    }

    @Override
    public void onBindViewHolder(@NotNull VH holder, boolean lastItem) {
        holder.bindData(this, lastItem);
    }

//    public void setNickNames(Map<String, String> nickNames) {
//        this.nickNames = nickNames;
//    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MessageBaseItem) {
            return ((MessageBaseItem) obj).message.equals(message);
        }
        return false;
    }

    public static abstract class ViewHolder extends BaseMessageViewHolder
            implements View.OnClickListener, RevealableViewHolder {
        private Map<String, String> nickNames = new HashMap<>();
        protected RadioButton rbSelection;
        protected ImageView senderProfileImage;
        protected TextView tvMessageInfo;
        protected TextView tvStatus;
        protected TextView revealableView;

        protected MessageBaseItem item;
        protected boolean maskStatus;
        public boolean lastItem;
        private float mInitialTranslateX = ResourceUtils.dpToPx(80);

        protected MessageListener messageListener;

        public ViewHolder(@Nullable View itemView) {
            super(itemView);
            rbSelection = itemView.findViewById(R.id.item_chat_select);
            tvMessageInfo = itemView.findViewById(R.id.item_chat_info);
            tvStatus = itemView.findViewById(R.id.item_chat_status);
            senderProfileImage = itemView.findViewById(R.id.item_chat_user_profile);
            if (senderProfileImage != null) {
                senderProfileImage.setOnClickListener(this);
            }
            rbSelection.setOnClickListener(this);
            itemView.setOnClickListener(this);
            revealableView = itemView.findViewById(R.id.revealable_view);
        }

        public void setMessageListener(MessageListener messageListener) {
            this.messageListener = messageListener;
        }

        public void setNickNames(Map<String, String> nickNames) {
            this.nickNames = nickNames;
        }

        public void bindData(MessageBaseItem item, boolean lastItem) {
            this.item = item;
            this.lastItem = lastItem;
            this.maskStatus = CommonMethod.getBooleanFrom(item.message.markStatuses, item.message.currentUserId);
            rbSelection.setVisibility(item.isEditMode ? View.VISIBLE : View.GONE);
            rbSelection.setChecked(item.isSelected);
            rbSelection.setSelected(item.isSelected);
            setSenderImage(item.message);
            setMessageInfo(item.message);
            setMessageStatus(item.message, lastItem);
            transform(ChatMessageAdapter.xDiff);
        }

        @Override
        protected boolean handleTouchEvent(MotionEvent motionEvent) {
            if (item.isEditMode) {
                return false;
            }
            return super.handleTouchEvent(motionEvent);
        }

        @Override
        public void onSingleTap() {
            if (item.isEditMode) {
                handleSelection();
            }
        }

        @Override
        public void onDoubleTap() {

        }

        @Override
        public void onLongPress() {
            rbSelection.setChecked(true);
            itemView.postDelayed(() -> {
                TransitionManager.endTransitions((ViewGroup) itemView);
                TransitionManager.beginDelayedTransition((ViewGroup) itemView);
                rbSelection.setVisibility(View.VISIBLE);
            }, 10);
            if (messageListener != null) {
                messageListener.onLongPress(item);
            }
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.item_chat_select || item.isEditMode) {
                handleSelection();
            } else {
                switch (view.getId()) {
                    case R.id.item_chat_user_profile:
                        handleProfileImagePress();
                        break;
                    default:
                        break;
                }
            }
        }

        @Override
        public View getRevealView() {
            return revealableView;
        }

        @Override
        public View getSlideView() {
            return itemView;
        }

        @Override
        public RevealStyle getRevealStyle() {
            if (item != null) {
                return item.message.isFromMe() ? RevealStyle.SLIDE : RevealStyle.OVER;
            }
            return RevealStyle.OVER;
        }

        @Override
        public void transform(float xDiff) {
            if (getRevealView() != null) {
                float finalDistance = Math.max(xDiff, -mInitialTranslateX);
                if (finalDistance > 0)
                    return;

                View revealView = getRevealView();
                revealView.setTranslationX(mInitialTranslateX + finalDistance);
                if (getRevealStyle() == RevealStyle.SLIDE) {
                    View slideView = getSlideView();
                    if (slideView != null) {
                        slideView.setTranslationX(finalDistance);
                    }
                }
            }
        }

        private void handleSelection() {
            boolean toggleStatus = !rbSelection.isSelected();
            item.setSelected(toggleStatus);
            rbSelection.setChecked(toggleStatus);
            rbSelection.setSelected(toggleStatus);
            if (messageListener != null) {
                if (rbSelection.isChecked()) {
                    messageListener.selectMessage(item);
                } else {
                    messageListener.unSelectMessage(item);
                }
            }
        }

        private void setMessageInfo(Message message) {
            String time = DateUtils.toString("h:mm a", message.timestamp);
            if (revealableView != null) {
                revealableView.setText(time);
            }
            if (tvMessageInfo != null) {
                if (!message.showExtraInfo || item.conversationType != Constant.CONVERSATION_TYPE_GROUP) {
                    tvMessageInfo.setVisibility(View.GONE);
                } else {
                    tvMessageInfo.setVisibility(View.VISIBLE);
                    String nickName = (String) nickNames.get(message.senderId);
                    String senderName = message.sender != null ? message.sender.getDisplayName() : message.senderName;
                    tvMessageInfo.setText((TextUtils.isEmpty(nickName) ? senderName : nickName));
                }
            }
        }

        private void setSenderImage(Message message) {
            if (senderProfileImage == null) return;

            if (message.showExtraInfo) {
                senderProfileImage.setVisibility(View.VISIBLE);
                User sender = message.sender;
                if (sender != null) {
                    UiUtils.displayProfileImage(itemView.getContext(), senderProfileImage, sender);
                } else {
                    senderProfileImage.setImageResource(R.drawable.ic_avatar_gray);
                }
            } else {
                senderProfileImage.setVisibility(View.INVISIBLE);
            }
        }

        private void handleProfileImagePress() {
            if (messageListener != null) {
                if (item.message.sender != null) {
                    String imageName = "imageProfile" + getAdapterPosition();
                    Pair imagePair = Pair.create(senderProfileImage, imageName);
                    messageListener.handleProfileImagePress(item.message.sender, imagePair);
                }
            }
        }

        private void setMessageStatus(Message message, boolean lastItem) {
            if (tvStatus == null) return;
            String messageStatus = message.messageStatus;
            if ((lastItem || message.messageType == Constant.MSG_TYPE_GAME) && !TextUtils.isEmpty(messageStatus)) {
                tvStatus.setText(messageStatus);
                tvStatus.setVisibility(View.VISIBLE);
                if (message.messageStatusCode == Constant.MESSAGE_STATUS_ERROR) {
                    tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.red));
                } else {
                    tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_color_grey));
                }
                return;
            }
            tvStatus.setVisibility(View.GONE);
        }
    }

    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public interface MessageListener {

        void handleProfileImagePress(User user, Pair<View, String>... sharedElements);

        void updateMessageMask(Message message, boolean markStatus, boolean lastItem);

        void onLongPress(MessageBaseItem messageItem);

        void openImage(String messageKey, String imageUrl, String localImage, boolean isPuzzled, Pair<View, String>... sharedElements);

        void openGameMessage(Message message);

        void onPauseAudioMessage(AudioMessageBaseItem message);

        void onCompletePlayAudio(AudioMessageBaseItem audioMessageBaseItem);

        void selectMessage(MessageBaseItem item);

        void unSelectMessage(MessageBaseItem item);
    }
}

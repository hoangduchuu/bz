package com.ping.android.presentation.view.flexibleitem.messages;

import android.support.annotation.CallSuper;
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

import com.bumptech.glide.RequestManager;
import com.bzzzchat.flexibleadapter.FlexibleItem;
import com.ping.android.R;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.model.enums.MessageType;
import com.ping.android.presentation.view.adapter.ChatMessageAdapter;
import com.ping.android.presentation.view.custom.revealable.RevealStyle;
import com.ping.android.presentation.view.custom.revealable.RevealableViewHolder;
import com.ping.android.presentation.view.flexibleitem.messages.audio.AudioMessageLeftItem;
import com.ping.android.presentation.view.flexibleitem.messages.audio.AudioMessageRightItem;
import com.ping.android.presentation.view.flexibleitem.messages.call.CallMessageLeftItem;
import com.ping.android.presentation.view.flexibleitem.messages.call.CallMessageRightItem;
import com.ping.android.presentation.view.flexibleitem.messages.groupimage.GroupImageMessageMessageLeftItem;
import com.ping.android.presentation.view.flexibleitem.messages.groupimage.GroupImageMessageRightItem;
import com.ping.android.presentation.view.flexibleitem.messages.image.ImageMessageLeftItem;
import com.ping.android.presentation.view.flexibleitem.messages.image.ImageMessageRightItem;
import com.ping.android.presentation.view.flexibleitem.messages.text.TextMessageLeftItem;
import com.ping.android.presentation.view.flexibleitem.messages.text.TextMessageRightItem;
import com.ping.android.presentation.view.flexibleitem.messages.video.VideoMessageLeftItem;
import com.ping.android.presentation.view.flexibleitem.messages.video.VideoMessageRightItem;
import com.ping.android.utils.CommonMethod;
import com.ping.android.utils.configs.Constant;
import com.ping.android.utils.DateUtils;
import com.ping.android.utils.ResourceUtils;
import com.ping.android.utils.UiUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tuanluong on 3/2/18.
 */

public abstract class MessageBaseItem<VH extends MessageBaseItem.ViewHolder> implements FlexibleItem<VH> {
    public Message message;
    public int conversationType;
    protected boolean isEditMode = false;
    protected boolean isSelected = false;

    public static MessageBaseItem from(Message message, String currentUserID, int conversationType) {
        MessageBaseItem baseItem;
        switch (message.type) {
            case IMAGE:
            case GAME:
                if (message.senderId.equals(currentUserID)) {
                    baseItem = new ImageMessageRightItem(message);
                } else {
                    baseItem = new ImageMessageLeftItem(message);
                }
                break;
            case VOICE:
                if (message.senderId.equals(currentUserID)) {
                    baseItem = new AudioMessageRightItem(message);
                } else {
                    baseItem = new AudioMessageLeftItem(message);
                }
                break;
            case VIDEO:
                if (message.senderId.equals(currentUserID)) {
                    baseItem = new VideoMessageRightItem(message);
                } else {
                    baseItem = new VideoMessageLeftItem(message);
                }
                break;
            case CALL:
                if (message.senderId.equals(currentUserID)) {
                    baseItem = new CallMessageRightItem(message);
                } else {
                    baseItem = new CallMessageLeftItem(message);
                }
                break;
            case IMAGE_GROUP:
                if (message.senderId.equals(currentUserID)) {
                    baseItem = new GroupImageMessageRightItem(message);
                } else {
                    baseItem = new GroupImageMessageMessageLeftItem(message);
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

        @Nullable
        protected MessageListener messageListener;
        protected RequestManager glide;

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

        @CallSuper
        public void bindData(MessageBaseItem item, boolean lastItem) {
            this.item = item;
            this.lastItem = lastItem;
            this.maskStatus = item.message.isMask;
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
            item.isSelected = true;
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
                    if (nickNames.containsKey(message.senderId)) {
                        tvMessageInfo.setText(nickNames.get(message.senderId));
                    } else {
                        tvMessageInfo.setText(message.senderName);
                    }
                }
            }
        }

        private void setSenderImage(Message message) {
            if (senderProfileImage == null) return;

            if (message.showExtraInfo) {
                senderProfileImage.setVisibility(View.VISIBLE);
                UiUtils.displayProfileAvatar(senderProfileImage, message.senderProfile, R.drawable.ic_avatar_gray);
            } else {
                senderProfileImage.setVisibility(View.INVISIBLE);
            }
        }

        private void handleProfileImagePress() {
            if (messageListener != null) {
                String imageName = "imageProfile" + getAdapterPosition();
                Pair imagePair = Pair.create(senderProfileImage, imageName);
                messageListener.onProfileImagePress(item.message.senderId, imagePair);
            }
        }

        private void setMessageStatus(Message message, boolean lastItem) {
            if (tvStatus == null) return;
            String messageStatus = message.messageStatus;
            if ((lastItem || message.type == MessageType.GAME) && !TextUtils.isEmpty(messageStatus)) {
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

        public void setGlide(RequestManager glide) {
            this.glide = glide;
        }
    }

    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public interface MessageListener {

        void onProfileImagePress(String senderId, Pair<View, String>... sharedElements);

        void updateMessageMask(Message message, boolean maskStatus, boolean lastItem);

        void onLongPress(MessageBaseItem messageItem);

        void openImage(Message message, boolean isPuzzled, Pair<View, String>... sharedElements);

        void openGameMessage(Message message);

        void onPauseAudioMessage(AudioMessageBaseItem message);

        void onCompletePlayAudio(AudioMessageBaseItem audioMessageBaseItem);

        void selectMessage(MessageBaseItem item);

        void unSelectMessage(MessageBaseItem item);

        void openVideo(@NotNull String videoUrl);

        void onCall(boolean isVideo);

        void onGroupImageItemPress(GroupImageMessageBaseItem.ViewHolder viewHolder, @NotNull List<Message> data, int position, Pair<View, String>... sharedElements);

        void updateChildMessageMask(Message message, boolean maskStatus);
    }
}

package com.ping.android.presentation.view.flexibleitem.messages;

import android.graphics.drawable.Drawable;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ping.android.activity.R;
import com.ping.android.model.Message;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.UiUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by tuanluong on 3/2/18.
 */

public abstract class ImageMessageBaseItem extends MessageBaseItem {
    public ImageMessageBaseItem(Message message) {
        super(message);
    }

    @NotNull
    @Override
    public ImageMessageBaseItem.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(getLayoutId(), parent, false);
        return new ImageMessageBaseItem.ViewHolder(view);
    }

    public static class ViewHolder extends MessageBaseItem.ViewHolder {
        ImageView imageView;
        private boolean isUpdated;

        public ViewHolder(@Nullable View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_chat_image);

            initGestureListener();
        }

        @Override
        protected View getClickableView() {
            return imageView;
        }

        @Override
        public void onDoubleTap() {
            if (item.isEditMode) {
                return;
            }
            if (item.message.messageType == Constant.MSG_TYPE_GAME) {
                if (item.message.messageStatusCode != Constant.MESSAGE_STATUS_GAME_PASS
                        && !item.message.isFromMe()) {
                    return;
                }
            }
            maskStatus = !maskStatus;
            if (messageListener != null) {
                messageListener.updateMessageMask(item.message, maskStatus, lastItem);
            }
        }

        @Override
        public void onSingleTap() {
            if (item.isEditMode) {
                return;
            }
            switch (item.message.messageType) {
                case Constant.MSG_TYPE_IMAGE:
                    handleImagePress(maskStatus);
                    break;
                case Constant.MSG_TYPE_GAME:
                    handleGamePress(maskStatus);
                    break;
            }
        }

        @Override
        public void bindData(MessageBaseItem item, boolean lastItem) {
            isUpdated = false;
            if (this.item != null) {
                isUpdated = item.message.key.equals(this.item.message.key);
            }
            super.bindData(item, lastItem);
            setImageMessage(item.message);
        }

        private void handleImagePress(boolean isPuzzled) {
            if (TextUtils.isEmpty(item.message.localImage)) {
                String photoUrl = !TextUtils.isEmpty(item.message.photoUrl)
                        ? item.message.photoUrl : item.message.thumbUrl;
                if (TextUtils.isEmpty(photoUrl))
                    return;
                viewImage(photoUrl, "", isPuzzled);
            } else {
                viewImage("", item.message.localImage, isPuzzled);
            }
        }

        private void handleGamePress(boolean isPuzzled) {
            if (!TextUtils.isEmpty(item.message.gameUrl) && item.message.gameUrl.startsWith("PPhtotoMessageIdentifier")) {
                return;
            }
            if (TextUtils.isEmpty(item.message.gameUrl)) {
                return;
            }
            // Only play game for player
            int status = ServiceManager.getInstance().getCurrentStatus(item.message.status);
            if (!item.message.currentUserId.equals(item.message.senderId)) {
                if (status == Constant.MESSAGE_STATUS_GAME_PASS) {
                    // Game pass, just unpuzzle image
                    viewImage(item.message.gameUrl, "", isPuzzled);
                } else if (status != Constant.MESSAGE_STATUS_GAME_FAIL) {
                    if (messageListener != null) {
                        messageListener.openGameMessage(item.message);
                    }
                }
            } else {
                // Show image for current User
                viewImage(item.message.gameUrl, "", isPuzzled);
            }
        }

        private void viewImage(String imageUrl, String localUrl, boolean isPuzzled) {
            Pair imagePair = Pair.create(imageView, item.message.key);
            if (messageListener != null) {
                messageListener.openImage(item.message.key, imageUrl, localUrl, isPuzzled, imagePair);
            }
        }

        private void setImageMessage(Message message) {
            boolean bitmapMark = maskStatus;
            if (imageView == null) return;
            if (!TextUtils.isEmpty(item.message.localImage)) {
                UiUtils.loadImageFromFile(imageView, item.message.localImage, message.key, maskStatus);
                return;
            }

            String imageURL = message.photoUrl;
            if (item.message.messageType == Constant.MSG_TYPE_GAME) {
                imageURL = message.gameUrl;
            }
            if (TextUtils.isEmpty(imageURL) || imageURL.startsWith("PPhtotoMessageIdentifier")) {
                imageView.setImageResource(R.drawable.img_loading_image);
                return;
            }
            int status = ServiceManager.getInstance().getCurrentStatus(message.status);
            if (!TextUtils.isEmpty(message.gameUrl) && !message.currentUserId.equals(message.senderId)) {
                if (status == Constant.MESSAGE_STATUS_GAME_FAIL) {
                    imageView.setImageResource(R.drawable.img_game_over);
                    return;
                } else if (status != Constant.MESSAGE_STATUS_GAME_PASS) {
                    bitmapMark = true;
                }
            }
            String url = imageURL;
            if (imageURL.startsWith(Constant.IMAGE_PREFIX)) {
                url = imageURL.substring(Constant.IMAGE_PREFIX.length());
                UiUtils.loadImageFromFile(imageView, url, message.key, bitmapMark);
                return;
            }
            Drawable placeholder = null;
            if (isUpdated) {
                //placeholder = imageView.getDrawable();
            }
            UiUtils.loadImage(imageView, url, message.key, bitmapMark, placeholder);
        }
    }
}

package com.ping.android.presentation.view.flexibleitem.messages;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ping.android.R;
import com.ping.android.model.Callback;
import com.ping.android.model.Message;
import com.ping.android.model.enums.MessageType;
import com.ping.android.utils.CommonMethod;
import com.ping.android.utils.UiUtils;
import com.ping.android.utils.configs.Constant;

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
        private ImageView imageView;
        private boolean isUpdated;
        private View loadingView;

        public ViewHolder(@Nullable View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_chat_image);
            loadingView = itemView.findViewById(R.id.loading_container);
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
            if (item.message.type == MessageType.GAME) {
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
            switch (item.message.type) {
                case IMAGE:
                    handleImagePress(maskStatus);
                    break;
                case GAME:
                    handleGamePress(maskStatus);
                    break;
            }
        }

        @Override
        public void bindData(MessageBaseItem item, boolean lastItem) {
            isUpdated = false;
            if (this.item != null) {
                isUpdated = item.message.key.equals(this.item.message.key);
                if (isUpdated && !TextUtils.isEmpty(this.item.message.localFilePath)) {
                    item.message.localFilePath = this.item.message.localFilePath;
                }
            }
            super.bindData(item, lastItem);
            setImageMessage(item.message);
        }

        @Override
        public View getSlideView() {
            return imageView;
        }

        private void handleImagePress(boolean isPuzzled) {
            if (TextUtils.isEmpty(item.message.localFilePath)) {
                String photoUrl = !TextUtils.isEmpty(item.message.mediaUrl)
                        ? item.message.mediaUrl : item.message.thumbUrl;
                if (TextUtils.isEmpty(photoUrl) || photoUrl.startsWith("PPhtotoMessageIdentifier"))
                    return;
                viewImage(isPuzzled);
            } else {
                viewImage(isPuzzled);
            }
        }

        private void handleGamePress(boolean isPuzzled) {
            if (!TextUtils.isEmpty(item.message.mediaUrl) && item.message.mediaUrl.startsWith("PPhtotoMessageIdentifier")) {
                return;
            }
            if (TextUtils.isEmpty(item.message.mediaUrl)) {
                return;
            }
            // Only play game for player
            //int status = ServiceManager.getInstance().getCurrentStatus(item.message.status);
            if (!item.message.currentUserId.equals(item.message.senderId)) {
                if (item.message.messageStatusCode == Constant.MESSAGE_STATUS_GAME_PASS) {
                    // Game pass, just unpuzzle image
                    viewImage(isPuzzled);
                } else if (item.message.messageStatusCode != Constant.MESSAGE_STATUS_GAME_FAIL) {
                    if (messageListener != null) {
                        messageListener.openGameMessage(item.message);
                    }
                }
            } else {
                // Show image for current User
                viewImage(isPuzzled);
            }
        }

        private void viewImage(boolean isPuzzled) {
            Pair imagePair = Pair.create(imageView, item.message.key);
            if (messageListener != null) {
                messageListener.openImage(item.message, isPuzzled, imagePair);
            }
        }

        private void setImageMessage(Message message) {
            boolean bitmapMark = maskStatus;
            if (imageView == null) return;
            if (!TextUtils.isEmpty(item.message.localFilePath)) {
                UiUtils.loadImageFromFile(imageView, item.message.localFilePath, message.key, maskStatus);
                loadingView.setVisibility(View.GONE);
                return;
            }

            String imageURL = message.mediaUrl;
            if (TextUtils.isEmpty(imageURL) || imageURL.startsWith("PPhtotoMessageIdentifier")) {
                imageView.setImageResource(R.drawable.img_loading_image);
                return;
            }
            if (message.messageStatusCode == Constant.MESSAGE_STATUS_GAME_FAIL) {
                imageView.setImageResource(R.drawable.img_game_over);
                loadingView.setVisibility(View.GONE);
                return;
            }
            String url = imageURL;
            Callback callback = (error, data) -> {
                if (error == null) {
                    imageView.setImageBitmap((Bitmap) data[0]);
                }
                loadingView.setVisibility(View.GONE);
            };
            loadingView.setVisibility(View.VISIBLE);
            if (imageURL.startsWith(Constant.IMAGE_PREFIX)) {
                url = imageURL.substring(Constant.IMAGE_PREFIX.length());
                UiUtils.loadImageFromFile(imageView, url, message.key, bitmapMark, callback);
                return;
            }
            Drawable placeholder = null;
            if (isUpdated) {
                placeholder = imageView.getDrawable();
            }
            UiUtils.loadImage(imageView, url, message.key, bitmapMark, placeholder, callback);
        }
    }
}

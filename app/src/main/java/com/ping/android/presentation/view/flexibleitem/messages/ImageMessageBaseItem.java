package com.ping.android.presentation.view.flexibleitem.messages;

import android.graphics.Outline;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bzzzchat.configuration.GlideRequest;
import com.bzzzchat.configuration.GlideRequests;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ping.android.R;
import com.ping.android.model.Message;
import com.ping.android.model.enums.MessageType;
import com.ping.android.utils.ResourceUtils;
import com.ping.android.utils.configs.Constant;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

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
            int radius = ResourceUtils.dpToPx(20);
            imageView.setClipToOutline(true);
            imageView.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
                }
            });
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
                if (TextUtils.isEmpty(photoUrl))
                    return;
                viewImage(isPuzzled);
            } else {
                viewImage(isPuzzled);
            }
        }

        private void handleGamePress(boolean isPuzzled) {
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
            Drawable placeholder = ContextCompat.getDrawable(imageView.getContext(), R.drawable.img_loading_image);
            if (isUpdated) {
                placeholder = imageView.getDrawable();
            }
            if (!TextUtils.isEmpty(item.message.localFilePath)) {
                ((GlideRequests) this.glide)
                        .load(item.message.localFilePath)
                        .placeholder(placeholder)
                        .dontAnimate()
                        .messageImage(message.key, bitmapMark)
                        .into(imageView);
                // should preload remote image
                if (!TextUtils.isEmpty(message.mediaUrl) && message.mediaUrl.startsWith("gs://")) {
                    StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(message.mediaUrl);
                    ((GlideRequests) this.glide).load(gsReference)
                            .messageImage(message.key, bitmapMark)
                            .preload();
                }
                loadingView.setVisibility(View.GONE);
                return;
            }

            String imageURL = message.mediaUrl;
            if (TextUtils.isEmpty(imageURL)) {
                imageView.setImageResource(R.drawable.img_loading_image);
                return;
            }
            if (message.messageStatusCode == Constant.MESSAGE_STATUS_GAME_FAIL) {
                imageView.setImageResource(R.drawable.img_game_over);
                loadingView.setVisibility(View.GONE);
                return;
            }
            loadingView.setVisibility(View.VISIBLE);

            GlideRequest<Drawable> request = null;
            if (imageURL.startsWith("gs://")) {
                StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageURL);
                request = ((GlideRequests) this.glide)
                        .load(gsReference);
            } else {
                request = ((GlideRequests) this.glide)
                        .load(new File(imageURL));
            }

            request.placeholder(placeholder)
                    .messageImage(message.key, bitmapMark)
                    .dontAnimate()
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@androidx.annotation.Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            loadingView.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            loadingView.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(imageView);
        }
    }
}

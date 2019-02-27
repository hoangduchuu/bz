package com.ping.android.presentation.view.flexibleitem.messages;

import android.app.Activity;
import android.graphics.Outline;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.core.util.Pair;

import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
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
import com.ping.android.utils.Log;
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
        private FrameLayout content;
        private ImageView imageView;
        private boolean isUpdated;
        private View loadingView;
        private int width = 0;

        public ViewHolder(@Nullable View itemView) {
            super(itemView);

            content = itemView.findViewById(R.id.content);
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
            width = getFullWidth();
            scaleLoadingViewHolder();
        }

        @Override
        protected View getClickableView() {
            return imageView;
        }

        /**
         * get Width of device
         */
        private int getFullWidth(){
            DisplayMetrics displayMetrics = new DisplayMetrics();

            ((Activity) itemView.getContext() ).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            return displayMetrics.widthPixels;
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
            return content;
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
            if (messageListener != null && (!faceIdStatusRepository.isFaceIdEnabled() || faceIdStatusRepository.getFaceIdRecognitionStatus().get())) {
                messageListener.openImage(item.message, isPuzzled, imagePair);
            }
        }

        private void setImageMessage(Message message) {
            boolean bitmapMark = maskStatus;
            if (!bitmapMark || (faceIdStatusRepository.isFaceIdEnabled() && faceIdStatusRepository.getFaceIdRecognitionStatus().get())) {
                if (item.message.type == MessageType.GAME) {
                    if (item.message.messageStatusCode != Constant.MESSAGE_STATUS_GAME_PASS
                            && !item.message.isFromMe()) {
                        bitmapMark = true;
                    }else{
                        bitmapMark = false;
                    }
                }else {
                    bitmapMark = false;
                }
            }else{
                bitmapMark = true;
            }
            if (imageView == null) return;
            //Drawable placeholder = ContextCompat.getDrawable(imageView.getContext(), R.drawable.img_loading_image);
            if (!isUpdated) {
                loadingView.setVisibility(View.VISIBLE);
            }
            if (!TextUtils.isEmpty(item.message.localFilePath)) {
                ((GlideRequests) this.glide)
                        .load(item.message.localFilePath)
//                        .placeholder(placeholder)
                        .dontAnimate()
                        .messageImage(message.key, bitmapMark)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@androidx.annotation.Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                loadingView.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                loadingView.setVisibility(View.GONE);
                                float w = ((BitmapDrawable) resource).getBitmap().getWidth();
                                float h = ((BitmapDrawable) resource).getBitmap().getHeight();
                                float parentWith = width;
                                calculateImageViewSize(w,h,parentWith);
                                return false;
                            }
                        })
                        .into(imageView);
                // should preload remote image
                if (!TextUtils.isEmpty(message.mediaUrl) && message.mediaUrl.startsWith("gs://")) {
                    StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(message.mediaUrl);
                    ((GlideRequests) this.glide).load(gsReference)
                            .messageImage(message.key, bitmapMark)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@androidx.annotation.Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    loadingView.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    loadingView.setVisibility(View.GONE);
                                    float w = ((BitmapDrawable) resource).getBitmap().getWidth();
                                    float h = ((BitmapDrawable) resource).getBitmap().getHeight();
                                    float parentWith = width;
                                    calculateImageViewSize(w,h,parentWith);
                                    return false;
                                }
                            })
                            .preload();
                }
                loadingView.setVisibility(View.GONE);
                return;
            }

            String imageURL = message.mediaUrl;
            if (TextUtils.isEmpty(imageURL)) {
                imageView.setImageResource(0);
                return;
            }
            if (message.messageStatusCode == Constant.MESSAGE_STATUS_GAME_FAIL) {
                Glide.with(itemView.getContext())
                        .load(R.drawable.img_game_over)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@androidx.annotation.Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                loadingView.setVisibility(View.GONE);
                                float w = ((BitmapDrawable) resource).getBitmap().getWidth();
                                float h = ((BitmapDrawable) resource).getBitmap().getHeight();
                                float parentWith = width;
                                calculateImageViewSize(w,h,parentWith);
                                return false;
                            }
                        }).into(imageView);
                loadingView.setVisibility(View.GONE);
                return;
            }

            GlideRequest<Drawable> request = null;
            if (imageURL.startsWith("gs://")) {
                StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageURL);
                request = ((GlideRequests) this.glide)
                        .load(gsReference)                    .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@androidx.annotation.Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                loadingView.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                loadingView.setVisibility(View.GONE);
                                float w = ((BitmapDrawable) resource).getBitmap().getWidth();
                                float h = ((BitmapDrawable) resource).getBitmap().getHeight();
                                float parentWith = width;
                                calculateImageViewSize(w,h,parentWith);
                                return false;
                            }
                        });
            } else {
                request = ((GlideRequests) this.glide)
                        .load(new File(imageURL))
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@androidx.annotation.Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                loadingView.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                loadingView.setVisibility(View.GONE);
                                float w = ((BitmapDrawable) resource).getBitmap().getWidth();
                                float h = ((BitmapDrawable) resource).getBitmap().getHeight();
                                float parentWith = width;
                                calculateImageViewSize(w,h,parentWith);
                                return false;
                            }
                        });
            }

            request
//                    .placeholder(placeholder)
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
                            float w = ((BitmapDrawable) resource).getBitmap().getWidth();
                            float h = ((BitmapDrawable) resource).getBitmap().getHeight();
                            float parentWith = width;
                            calculateImageViewSize(w,h,parentWith);
                            return false;
                        }
                    })
                    .into(imageView);
        }

        private void calculateImageViewSize(float w, float h, float parentWidth) {
            if (w>h){
                int imageViewWidth = (int) (70 * parentWidth /100);
                int imageViewHeight = (int) (imageViewWidth * (h/w));
                ViewGroup.LayoutParams params = imageView.getLayoutParams();
                params.width = imageViewWidth;
                params.height = imageViewHeight;
                imageView.setLayoutParams(params);
            }else {
                int imageViewHeight = (int) (70 * parentWidth /100);
                int imageViewWidth = (int) (imageViewHeight * (w/h));
                ViewGroup.LayoutParams params = imageView.getLayoutParams();
                params.width = imageViewWidth;
                params.height = imageViewHeight;
                imageView.setLayoutParams(params);
            }
        }

        /**
         * Scale ImageView Holder
         */
        private void scaleLoadingViewHolder() {
            int holderHeight = 70 * width / 100;
            int holderWidth = 4 * holderHeight / 6;
            ViewGroup.LayoutParams params = loadingView.getLayoutParams();
            params.height = holderHeight;
            params.width = holderWidth;
            loadingView.setLayoutParams(params);
        }
    }
}

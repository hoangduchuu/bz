package com.bzzzchat.configuration;

import com.bumptech.glide.annotation.GlideExtension;
import com.bumptech.glide.annotation.GlideOption;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;

import androidx.annotation.NonNull;

@GlideExtension
public class BzzzGlideExtension {
    private BzzzGlideExtension() {
    }

    @GlideOption
    @NonNull
    public static RequestOptions messageImage(RequestOptions options, String messageKey, boolean isMask) {
        return options
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(512)
                .transform(new BitmapEncode(isMask))
                .signature(new ObjectKey(String.format("%s%s", messageKey, isMask ? "encoded" : "decoded")));
    }

    @GlideOption
    @NonNull
    public static RequestOptions profileImage(RequestOptions options) {
        return options
                .override(128)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_avatar_gray)
                .error(R.drawable.ic_avatar_gray)
                .apply(RequestOptions.circleCropTransform());
    }
}

package com.bzzzchat.configuration;

import com.bumptech.glide.annotation.GlideExtension;
import com.bumptech.glide.annotation.GlideOption;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;

@GlideExtension
public class BzzzGlideExtension {
    private BzzzGlideExtension() {
    }

    @GlideOption
    public static void messageImage(RequestOptions options, String messageKey, boolean isMask) {
        options
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(512)
                .transform(new BitmapEncode(isMask))
                .signature(new ObjectKey(String.format("%s%s", messageKey, isMask ? "encoded" : "decoded")));
    }

    @GlideOption
    public static void profileImage(RequestOptions options) {
        options
                .override(128)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_avatar_gray)
                .error(R.drawable.ic_avatar_gray)
                .apply(RequestOptions.circleCropTransform());
    }
}

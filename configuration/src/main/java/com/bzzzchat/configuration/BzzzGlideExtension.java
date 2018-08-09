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
                .override(500)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(512)
                .transform(new BitmapEncode(isMask))
                .signature(new ObjectKey(String.format("%s%s", messageKey, isMask ? "encoded" : "decoded")));
    }
}

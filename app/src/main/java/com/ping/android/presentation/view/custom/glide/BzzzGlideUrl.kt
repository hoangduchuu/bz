package com.ping.android.presentation.view.custom.glide

import com.bumptech.glide.load.model.GlideUrl

class BzzzGlideUrl(url: String, val messageKey: String): GlideUrl(url) {
    override fun getCacheKey(): String {
        return messageKey
    }

    override fun toString(): String {
        return cacheKey
    }
}
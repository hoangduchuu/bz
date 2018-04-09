package com.ping.android.model

import com.ping.android.presentation.view.adapter.AdapterConstants
import com.ping.android.presentation.view.adapter.ViewType

data class GalleryItem(var imageUrl: Int): ViewType {

    override fun getViewType(): Int {
        return AdapterConstants.IMAGE
    }
}
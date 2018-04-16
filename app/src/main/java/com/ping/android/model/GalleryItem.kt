package com.ping.android.model

import com.ping.android.presentation.view.adapter.AdapterConstants
import com.ping.android.presentation.view.adapter.ViewType

data class GalleryItem(var imageUrl: Int) : ViewType {

    override fun getViewType(): Int {
        return AdapterConstants.IMAGE
    }
}

data class FirebaseImageItem(var imageUrl: String): ViewType {
    override fun getViewType(): Int {
        return AdapterConstants.IMAGE
    }
}

data class LocalBackgroundItem(var resId: Int) : ViewType {

    override fun getViewType(): Int {
        return AdapterConstants.IMAGE
    }
}
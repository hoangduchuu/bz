package com.ping.android.model

import com.ping.android.presentation.view.adapter.AdapterConstants
import com.ping.android.presentation.view.adapter.ViewType

class BlankItem: ViewType {
    override fun getViewType(): Int {
        return AdapterConstants.BLANK
    }
}

class GalleryItem : ViewType {
    override fun getViewType(): Int {
        return AdapterConstants.GALLERY
    }
}

class CameraItem : ViewType {
    override fun getViewType(): Int {
        return AdapterConstants.CAMERA
    }
}

data class FirebaseImageItem(var imageUrl: String) : ViewType {
    override fun getViewType(): Int {
        return AdapterConstants.IMAGE
    }
}

data class LocalBackgroundItem(var resId: Int) : ViewType {

    override fun getViewType(): Int {
        return AdapterConstants.IMAGE
    }
}

data class ImageMessage(var message: Message) : ViewType {
    override fun getViewType(): Int {
        return AdapterConstants.IMAGE
    }
}
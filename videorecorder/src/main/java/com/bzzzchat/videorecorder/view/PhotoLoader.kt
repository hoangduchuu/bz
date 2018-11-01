package com.bzzzchat.videorecorder.view

import android.content.Context
import android.content.CursorLoader
import android.provider.MediaStore

class PhotoLoader(context: Context): CursorLoader(context) {
    init {
        projection = null
        uri = MediaStore.Files.getContentUri("external")
        sortOrder = "${MediaStore.Images.Media._ID} DESC"
        selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE}=${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE}"
    }
}
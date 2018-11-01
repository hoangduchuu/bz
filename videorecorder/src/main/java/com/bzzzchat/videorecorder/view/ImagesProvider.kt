package com.bzzzchat.videorecorder.view

import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import android.provider.MediaStore

data class PhotoItem(val imageId: Int,
                     val thumbnailPath: String,
                     val imagePath: String,
                     var isSelected: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readByte() != 0.toByte())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(imageId)
        parcel.writeString(thumbnailPath)
        parcel.writeString(imagePath)
        parcel.writeByte(if (isSelected) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PhotoItem> {
        override fun createFromParcel(parcel: Parcel): PhotoItem {
            return PhotoItem(parcel)
        }

        override fun newArray(size: Int): Array<PhotoItem?> {
            return arrayOfNulls(size)
        }
    }
}

class ImagesProvider(val activity: Activity) {
    val photoId = 111

    fun getPhotoDirs(resultCallback: (List<PhotoItem>) -> Unit) {
        if (activity.loaderManager.getLoader<Cursor>(photoId) != null) {
            activity.loaderManager.restartLoader<Cursor>(photoId, null, PhotoLoaderCallbacks(activity, resultCallback))
        } else {
            activity.loaderManager.initLoader(photoId, null, PhotoLoaderCallbacks(activity, resultCallback))
        }
    }

//    fun getThumbnail(imageId: String): String {
//        val projection = arrayOf(MediaStore.Images.Thumbnails.DATA, MediaStore.Images.Thumbnails.IMAGE_ID)
//        val order = "${MediaStore.Images.Thumbnails.IMAGE_ID} DESC"
//        val selection = "${MediaStore.Images.Thumbnails.IMAGE_ID}=?"
//        val selectionArgs = arrayOf(imageId)
//        val cursor = activity.contentResolver.query(
//                MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
//                projection,
//                selection,
//                selectionArgs,
//                null)
//        try {
//            if (cursor.moveToFirst()) {
//                val columnIndex = cursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA)
//                return cursor.getString(columnIndex)
//            }
//            return ""
//        } finally {
//            cursor.close()
//        }
//    }

//    fun getImages(context: Context): List<PhotoItem> {
//        val projection = arrayOf(MediaStore.Images.Thumbnails.DATA, MediaStore.Images.Thumbnails.IMAGE_ID)
//        val order = "${MediaStore.Images.Thumbnails.IMAGE_ID} DESC"
//        val cursor = context.contentResolver.query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
//                projection,
//                null,
//                null,
//                order)
//        val thumbnailIndex = cursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA)
//        val result = ArrayList<PhotoItem>(cursor.count)
//        if (cursor.moveToFirst()) {
//            do {
//                val id = cursor.getInt(thumbnailIndex)
//                val thumbnailPath = cursor.getString(id)
//                val imageId = cursor.getString(cursor.getColumnIndex(projection[1]))
//                val imagePath = getImagePath(context, imageId)
//                result.add(PhotoItem(imageId, thumbnailPath, imagePath))
//            } while (cursor.moveToNext())
//        }
//        cursor.close()
//        return result
//    }

//    fun getImagePath(context: Context, imageId: String): String {
//        val imagePathColumns = arrayOf(MediaStore.Images.Media.DATA)
//        val selection = "${MediaStore.Images.Media._ID}=?"
//        val selectionArgs = arrayOf(imageId)
//        val cursor = context.contentResolver.query(
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                imagePathColumns,
//                selection,
//                selectionArgs,
//                null)
//        try {
//            if (cursor.moveToFirst()) {
//                val columnIndex = cursor.getColumnIndex(imagePathColumns[0])
//                return cursor.getString(columnIndex)
//            }
//            return ""
//        } finally {
//            cursor.close()
//        }
//    }
}

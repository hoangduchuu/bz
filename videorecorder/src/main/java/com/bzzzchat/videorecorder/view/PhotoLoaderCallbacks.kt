package com.bzzzchat.videorecorder.view

import android.app.LoaderManager
import android.content.Context
import android.content.Loader
import android.database.Cursor
import android.os.Bundle
import android.provider.BaseColumns
import android.provider.MediaStore

class PhotoLoaderCallbacks(val context: Context, val resultCallback: (List<PhotoItem>) -> Unit): LoaderManager.LoaderCallbacks<Cursor> {
    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> = PhotoLoader(context)

    override fun onLoadFinished(loader: Loader<Cursor>?, data: Cursor?) {
        data?.let {
            val thumbnailIndex = it.getColumnIndex(MediaStore.Images.Thumbnails.DATA)
            val result = ArrayList<PhotoItem>(it.count)
            if (it.moveToFirst()) {
                do {
                    val imageId = it.getInt(it.getColumnIndexOrThrow(BaseColumns._ID))
                    val path = it.getString(it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
                    val thumbnail = getThumnail(imageId.toString())
//                    val imageId = it.getString(it.getColumnIndex(MediaStore.Images.Thumbnails.IMAGE_ID))
//                    //val imagePath = ImagesProvider.getImagePath(context, imageId)
                    result.add(PhotoItem(imageId, thumbnail, path))
                } while (it.moveToNext())
            }
            it.close()
            resultCallback(result)
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>?) {

    }

    fun getThumnail(imageId: String): String {
        val projection = arrayOf(MediaStore.Images.Thumbnails.DATA, MediaStore.Images.Thumbnails.IMAGE_ID)
        val selection = "${MediaStore.Images.Thumbnails.IMAGE_ID}=?"
        val selectionArgs = arrayOf(imageId)
        val cursor = context.contentResolver.query(
                MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null)
        try {
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA)
                return cursor.getString(columnIndex)
            }
            return ""
        } finally {
            cursor.close()
        }
    }

}
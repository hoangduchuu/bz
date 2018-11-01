package com.ping.android.utils.extensions

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

fun Uri.simpleName(context: Context): String {
    context.contentResolver.query(this, null, null, null, null)?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        it.moveToFirst()
        return it.getString(nameIndex)
    }
    return ""
}
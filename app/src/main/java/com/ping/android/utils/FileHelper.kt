package com.ping.android.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.ping.android.BuildConfig
import java.io.File

fun File.uri(context: Context): Uri {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        FileProvider.getUriForFile(context,
                BuildConfig.APPLICATION_ID + ".provider",
                this)
    } else {
        Uri.fromFile(this)
    }
}
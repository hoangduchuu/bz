package com.ping.android.presentation.view.custom

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.core.view.inputmethod.EditorInfoCompat
import androidx.core.view.inputmethod.InputConnectionCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.vanniktech.emoji.EmojiEditText

interface MediaSelectionListener {
    fun onMediaSelected(uri: Uri)
}

class EmojiGifEditText : EmojiEditText {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    var listener: MediaSelectionListener? = null
    val availableMimeType = arrayOf("image/gif", "image/png", "image/jpeg")

    override fun onCreateInputConnection(editorInfo: EditorInfo): InputConnection {
        val ic = super.onCreateInputConnection(editorInfo)
        EditorInfoCompat.setContentMimeTypes(editorInfo, arrayOf("image/gif", "image/png"))
        val callback = InputConnectionCompat.OnCommitContentListener { inputContentInfo, flags, opts ->
            // read and display inputContentInfo asynchronously
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && (flags and InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION !== 0)) {
                try {
                    inputContentInfo.requestPermission()
                } catch (e: Exception) {
                    return@OnCommitContentListener false // return false if failed
                }

            }
            for (mimeType in availableMimeType) {
                //inputContentInfo.description.mim()
            }

            listener?.onMediaSelected(inputContentInfo.contentUri)
            // call inputContentInfo.releasePermission() as needed.
            inputContentInfo.releasePermission()
            true  // return true if succeeded
        }
        return InputConnectionCompat.createWrapper(ic, editorInfo, callback)
    }


}
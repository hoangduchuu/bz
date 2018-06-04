package com.ping.android.presentation.view.flexibleitem.messages.video

import com.ping.android.R
import com.ping.android.model.Message
import com.ping.android.presentation.view.flexibleitem.messages.VideoMessageBaseItem

class VideoMessageLeftItem(message: Message) : VideoMessageBaseItem(message) {
    override val layoutId: Int
        get() = R.layout.item_chat_left_video
}
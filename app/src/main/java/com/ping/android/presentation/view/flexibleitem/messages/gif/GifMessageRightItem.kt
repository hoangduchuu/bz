package com.ping.android.presentation.view.flexibleitem.messages.gif

import com.ping.android.R
import com.ping.android.model.Message
import com.ping.android.presentation.view.flexibleitem.messages.GifMessageBaseItem

class GifMessageRightItem(message: Message): GifMessageBaseItem(message) {
    override val layoutId: Int
        get() = R.layout.item_chat_right_gif

}
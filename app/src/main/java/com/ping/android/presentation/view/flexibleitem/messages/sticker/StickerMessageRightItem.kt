package com.ping.android.presentation.view.flexibleitem.messages.sticker

import com.ping.android.presentation.view.flexibleitem.messages.StickerMessageBaseItem
import com.ping.android.R
import com.ping.android.model.Message

class StickerMessageRightItem(message: Message): StickerMessageBaseItem(message) {
    override val layoutId: Int
        get() = R.layout.item_chat_right_sticker

}
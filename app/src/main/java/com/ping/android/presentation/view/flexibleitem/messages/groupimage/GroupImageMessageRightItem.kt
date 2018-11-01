package com.ping.android.presentation.view.flexibleitem.messages.groupimage

import com.ping.android.R
import com.ping.android.model.Message
import com.ping.android.presentation.view.flexibleitem.messages.GroupImageMessageBaseItem

class GroupImageMessageRightItem(message: Message): GroupImageMessageBaseItem(message) {
    override val layoutId: Int = R.layout.item_chat_right_img_group
}

class GroupImageMessageMessageLeftItem(message: Message): GroupImageMessageBaseItem(message) {
    override val layoutId: Int = R.layout.item_chat_left_img_group
}
package com.ping.android.presentation.view.flexibleitem.messages.call

import com.ping.android.R
import com.ping.android.model.Message
import com.ping.android.presentation.view.flexibleitem.messages.CallMessageBaseItem

class CallMessageRightItem(message: Message) : CallMessageBaseItem(message) {

    override val layoutId: Int
        get() = R.layout.item_chat_right_call
}

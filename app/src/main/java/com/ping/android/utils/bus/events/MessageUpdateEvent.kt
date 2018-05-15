package com.ping.android.utils.bus.events

import com.ping.android.model.Message

data class MessageUpdateEvent(var message: Message, var index: Int)
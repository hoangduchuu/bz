package com.ping.android.model

data class NotificationMessage(
        var message: String,
        var timestamp: Long,
        var senderId: String
) {
    var senderName: String = "${message.split(":")[0]}:"
    var displayMessage: String = message.substring(senderName.length)
}
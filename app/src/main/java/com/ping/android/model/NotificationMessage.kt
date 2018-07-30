package com.ping.android.model

data class NotificationMessage(
        var message: String,
        var timestamp: Long,
        var senderId: String
)
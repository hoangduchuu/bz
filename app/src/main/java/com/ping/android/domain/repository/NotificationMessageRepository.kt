package com.ping.android.domain.repository

import com.ping.android.model.NotificationMessage

interface NotificationMessageRepository {
    fun getMessages(conversationId: String): List<NotificationMessage>
    fun addMessage(conversationId: String, message: NotificationMessage)
    fun clearMessages(conversationId: String)
    fun clearAll()
}
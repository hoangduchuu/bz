package com.ping.android.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ping.android.domain.repository.NotificationMessageRepository
import com.ping.android.model.NotificationMessage

class NotificationMessageRepositoryImpl(context: Context): NotificationMessageRepository {
    private val gson = Gson()
    private val sharedPreferences = context.getSharedPreferences("cachemessages", Context.MODE_PRIVATE)

    override fun getMessages(conversationId: String): List<NotificationMessage> {
        val cacheMessageString = sharedPreferences.getString(conversationId, "")
        return decodeMessages(cacheMessageString)
    }

    override fun addMessage(conversationId: String, message: NotificationMessage) {
        val messages: MutableList<NotificationMessage> = ArrayList(getMessages(conversationId))
        messages.add(message)
        val messageJson = encodeMessages(messages)
        sharedPreferences.edit().putString(conversationId, messageJson).apply()
    }

    override fun clearMessages(conversationId: String) {
        sharedPreferences.edit().putString(conversationId, "").apply()
    }

    override fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }

    private fun decodeMessages(messageJson: String): List<NotificationMessage> {
        return if (messageJson.isNotEmpty()) {
            val type = object : TypeToken<List<NotificationMessage>>() {}.type
            gson.fromJson(messageJson, type)
        } else {
            ArrayList()
        }
    }

    private fun encodeMessages(messages: List<NotificationMessage>): String {
        return gson.toJson(messages)
    }
}
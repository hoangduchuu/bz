package com.ping.android.data.mappers

import com.google.firebase.database.DataSnapshot
import com.ping.android.model.Conversation
import com.ping.android.model.User
import com.ping.android.model.enums.MessageCallType
import com.ping.android.model.enums.MessageType
import com.ping.android.utils.CommonMethod
import javax.inject.Inject

class ConversationMapper @Inject constructor() {
    fun transform(dataSnapshot: DataSnapshot, user: User): Conversation {
        val conversation = dataSnapshot.getValue(Conversation::class.java)!!
        conversation.key = dataSnapshot.key
        conversation.currentUserId = user.key
        conversation.deleteTimestamp = CommonMethod.getDoubleFrom(conversation.deleteTimestamps, user.key)
        conversation.isRead = CommonMethod.getBooleanFrom(conversation.readStatuses, user.key)
        conversation.currentColor = conversation.getColor(user.key)
        val maskStatus = CommonMethod.getBooleanFrom(conversation.markStatuses, user.key)
        val maskMessage = CommonMethod.getBooleanFrom(conversation.maskMessages, user.key)
        conversation.isMask = maskStatus// || maskMessage
        conversation.type = MessageType.from(conversation.messageType)
        conversation.messageCallType = MessageCallType.from(conversation.callType)
        return conversation
    }
}
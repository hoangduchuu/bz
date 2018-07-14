package com.ping.android.data.mappers

import android.text.TextUtils
import com.google.firebase.database.DataSnapshot
import com.ping.android.model.Message
import com.ping.android.model.User
import com.ping.android.model.enums.MessageCallType
import com.ping.android.model.enums.MessageType
import com.ping.android.utils.CommonMethod
import com.ping.android.utils.DataSnapshotWrapper
import com.ping.android.utils.configs.Constant
import junit.framework.Assert
import java.util.ArrayList
import java.util.HashMap
import javax.inject.Inject

class MessageMapper @Inject constructor() {
    fun transform(dataSnapshot: DataSnapshot, user: User): Message {
        val message = Message()
        val wrapper = DataSnapshotWrapper(dataSnapshot)
        message.message = wrapper.getStringValue("message")
        message.photoUrl = wrapper.getStringValue("photoUrl")
        message.thumbUrl = wrapper.getStringValue("thumbUrl")
        message.audioUrl = wrapper.getStringValue("audioUrl")
        message.gameUrl = wrapper.getStringValue("gameUrl")
        message.videoUrl = wrapper.getStringValue("videoUrl")
        message.messageType = wrapper.getIntValue("messageType", Constant.MSG_TYPE_TEXT)
        message.type = MessageType.from(message.messageType)
        message.timestamp = wrapper.getDoubleValue("timestamp", 0.0)
        message.senderId = wrapper.getStringValue("senderId")
        message.senderName = wrapper.getStringValue("senderName")
        message.gameType = wrapper.getIntValue("gameType", 0)
        message.voiceType = wrapper.getIntValue("voiceType", 0)
        message.callType = wrapper.getIntValue("callType", 0)
        message.callDuration = wrapper.getIntValue("callDuration", 0).toDouble()
        message.messageCallType = MessageCallType.from(message.callType)
        message.days = (message.timestamp * 1000 / Constant.MILLISECOND_PER_DAY).toLong()
        message.status = HashMap()
        val status = dataSnapshot.child("status").value as Map<String, Any>
        for (k in status.keys) {
            val value = status[k]
            var intValue = 0
            if (value is Long) {
                intValue = value.toInt()
            }
            message.status[k] = intValue
        }

        message.markStatuses = dataSnapshot.child("markStatuses").value as Map<String, Boolean>
        message.isMask = CommonMethod.getBooleanFrom(message.markStatuses, user.key)
        message.deleteStatuses = dataSnapshot.child("deleteStatuses").value as Map<String, Boolean>
        message.readAllowed = dataSnapshot.child("readAllowed").value as Map<String, Boolean>
        if (message.type === MessageType.IMAGE_GROUP) {
            val childMessageSnapshot = dataSnapshot.child("childMessages")
            val childMessages = ArrayList<Message>()
            if (childMessageSnapshot.exists()) {
                for (snapshot in childMessageSnapshot.children) {
                    val childMessage = transform(snapshot, user)
                    childMessage.parentKey = dataSnapshot.key
                    childMessage.isMask = CommonMethod.getBooleanFrom(childMessage.markStatuses, user.key)
                    childMessages.add(childMessage)
                }
            }
            message.childMessages = childMessages
        }

        message.currentUserId = user.key

        Assert.assertNotNull(message)
        message.key = dataSnapshot.key
        if (message.messageType == 0) {
            if (!TextUtils.isEmpty(message.message)) {
                message.messageType = Constant.MSG_TYPE_TEXT
            } else if (!TextUtils.isEmpty(message.photoUrl)) {
                message.messageType = Constant.MSG_TYPE_IMAGE
            } else if (!TextUtils.isEmpty(message.gameUrl)) {
                message.messageType = Constant.MSG_TYPE_GAME
            } else if (!TextUtils.isEmpty(message.audioUrl)) {
                message.messageType = Constant.MSG_TYPE_VOICE
            }
        }
        return message
    }
}
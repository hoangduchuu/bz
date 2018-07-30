package com.ping.android.data.mappers

import android.text.TextUtils
import android.view.View
import com.google.firebase.database.DataSnapshot
import com.ping.android.R
import com.ping.android.model.Message
import com.ping.android.data.entity.MessageEntity
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
    fun transform(dataSnapshot: DataSnapshot, user: User): MessageEntity {
        val message = MessageEntity()
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
        message.status = HashMap()
        val status = dataSnapshot.child("status").value as? Map<String, Any>
        status?.let {
            for (k in status.keys) {
                val value = status[k]
                var intValue = 0
                if (value is Long) {
                    intValue = value.toInt()
                }
                message.status[k] = intValue
            }
        }

        message.markStatuses = dataSnapshot.child("markStatuses").value as? Map<String, Boolean> ?: HashMap()
        message.isMask = CommonMethod.getBooleanFrom(message.markStatuses, user.key)
        message.deleteStatuses = dataSnapshot.child("deleteStatuses").value as? Map<String, Boolean>  ?: HashMap()
        message.readAllowed = dataSnapshot.child("readAllowed").value as? Map<String, Boolean>  ?: HashMap()
        if (message.type === MessageType.IMAGE_GROUP) {
            val childMessageSnapshot = dataSnapshot.child("childMessages")
            val childMessages = ArrayList<MessageEntity>()
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

    fun transform(entity: MessageEntity, currentUser: User): Message {
        val message = Message()
        message.key = entity.key
        message.message = entity.message
        message.mediaUrl = when (entity.messageType) {
            Constant.MSG_TYPE_IMAGE -> entity.photoUrl
            Constant.MSG_TYPE_GAME -> entity.gameUrl
            Constant.MSG_TYPE_VOICE -> entity.audioUrl
            Constant.MSG_TYPE_VIDEO -> entity.videoUrl
            else -> entity.photoUrl
        }
        message.thumbUrl = entity.thumbUrl
        message.type = MessageType.from(entity.messageType)
        message.timestamp = entity.timestamp
        message.senderId = entity.senderId
        message.senderName = entity.senderName
        message.gameType = entity.gameType
        message.voiceType = entity.voiceType
        message.callType = entity.callType
        message.callDuration = entity.callDuration
        message.messageCallType = MessageCallType.from(entity.callType)
        message.days = (entity.timestamp * 1000 / Constant.MILLISECOND_PER_DAY).toLong()
        message.status = entity.status

        prepareMessageStatus(message, currentUser)
        message.isMask = CommonMethod.getBooleanFrom(entity.markStatuses, currentUser.key)
        if (entity.gameType > 0 && message.senderId != currentUser.key) {
            if (message.messageStatusCode != Constant.MESSAGE_STATUS_GAME_PASS) {
                message.isMask = true
            }
        }
        if (message.type === MessageType.IMAGE_GROUP && entity.childMessages != null) {
            val childMessages = ArrayList<Message>()
            for (childEntity in entity.childMessages) {
                val childMessage = transform(childEntity, currentUser)
                childMessage.parentKey = entity.key
                childMessage.isMask = CommonMethod.getBooleanFrom(childEntity.markStatuses, currentUser.key)
                childMessages.add(childMessage)
            }
            childMessages.sortByDescending { it.timestamp }
            message.childMessages = childMessages
        }
        message.currentUserId = currentUser.key
        message.maskable = message.type == MessageType.TEXT
                || message.type == MessageType.IMAGE
                || message.type == MessageType.IMAGE_GROUP
                || (message.type == MessageType.GAME && message.isFromMe)
                || (message.type == MessageType.GAME && !message.isFromMe && message.messageStatusCode == Constant.MESSAGE_STATUS_GAME_PASS)
        return message
    }

    fun transform(dataSnapshot: DataSnapshot): MessageEntity {
        val message = MessageEntity()
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
        message.status = HashMap()
        val status = dataSnapshot.child("status").value as? Map<String, Any>
        status?.let {
            for (k in status.keys) {
                val value = status[k]
                var intValue = 0
                if (value is Long) {
                    intValue = value.toInt()
                }
                message.status[k] = intValue
            }
        }

        message.markStatuses = dataSnapshot.child("markStatuses").value as? Map<String, Boolean> ?: HashMap()
        message.deleteStatuses = dataSnapshot.child("deleteStatuses").value as? Map<String, Boolean>  ?: HashMap()
        message.readAllowed = dataSnapshot.child("readAllowed").value as? Map<String, Boolean>  ?: HashMap()
        if (message.type === MessageType.IMAGE_GROUP) {
            val childMessageSnapshot = dataSnapshot.child("childMessages")
            val childMessages = ArrayList<MessageEntity>()
            if (childMessageSnapshot.exists()) {
                for (snapshot in childMessageSnapshot.children) {
                    val childMessage = transform(snapshot)
                    childMessage.parentKey = dataSnapshot.key
                    childMessages.add(childMessage)
                }
            }
            childMessages.sortByDescending { it.timestamp }
            message.childMessages = childMessages
        }

        message.key = dataSnapshot.key
        return message
    }

//    fun transform(entity: MessageEntity, user: User): Message {
//        val message = Message()
//        message.key = entity.key
//        message.message = entity.message
//        message.photoUrl = entity.photoUrl
//        message.thumbUrl = entity.thumbUrl
//        message.audioUrl = entity.audioUrl
//        message.gameUrl = entity.gameUrl
//        message.videoUrl = entity.videoUrl
//        message.messageType = entity.messageType
//        message.type = MessageType.from(message.messageType)
//        message.timestamp = entity.timestamp
//        message.senderId = entity.senderId
//        message.senderName = entity.senderName
//        message.gameType = entity.gameType
//        message.voiceType = entity.voiceType
//        message.callType = entity.callType
//        message.callDuration = entity.callDuration
//        message.messageCallType = MessageCallType.from(message.callType)
//        message.days = (message.timestamp * 1000 / Constant.MILLISECOND_PER_DAY).toLong()
//        message.status = HashMap()
//        val status = dataSnapshot.child("status").value as? Map<String, Any>
//        status?.let {
//            for (k in status.keys) {
//                val value = status[k]
//                var intValue = 0
//                if (value is Long) {
//                    intValue = value.toInt()
//                }
//                message.status[k] = intValue
//            }
//        }
//
//        message.markStatuses = dataSnapshot.child("markStatuses").value as? Map<String, Boolean> ?: HashMap()
//        message.isMask = CommonMethod.getBooleanFrom(message.markStatuses, user.key)
//        message.deleteStatuses = dataSnapshot.child("deleteStatuses").value as? Map<String, Boolean>  ?: HashMap()
//        message.readAllowed = dataSnapshot.child("readAllowed").value as? Map<String, Boolean>  ?: HashMap()
//        if (message.type === MessageType.IMAGE_GROUP) {
//            val childMessageSnapshot = dataSnapshot.child("childMessages")
//            val childMessages = ArrayList<Message>()
//            if (childMessageSnapshot.exists()) {
//                for (snapshot in childMessageSnapshot.children) {
//                    val childMessage = transform(snapshot, user)
//                    childMessage.parentKey = dataSnapshot.key
//                    childMessage.isMask = CommonMethod.getBooleanFrom(childMessage.markStatuses, user.key)
//                    childMessages.add(childMessage)
//                }
//            }
//            childMessages.sortByDescending { it.timestamp }
//            message.childMessages = childMessages
//        }
//
//        message.currentUserId = user.key
//
//        Assert.assertNotNull(message)
//        if (message.messageType == 0) {
//            if (!TextUtils.isEmpty(message.message)) {
//                message.messageType = Constant.MSG_TYPE_TEXT
//            } else if (!TextUtils.isEmpty(message.photoUrl)) {
//                message.messageType = Constant.MSG_TYPE_IMAGE
//            } else if (!TextUtils.isEmpty(message.gameUrl)) {
//                message.messageType = Constant.MSG_TYPE_GAME
//            } else if (!TextUtils.isEmpty(message.audioUrl)) {
//                message.messageType = Constant.MSG_TYPE_VOICE
//            }
//        }
//        return message
//    }

    private fun prepareMessageStatus(message: Message, user: User) {
        var status = Constant.MESSAGE_STATUS_SENT
        for (statusValue in message.status.values) {
            if (statusValue == Constant.MESSAGE_STATUS_READ) {
                status = statusValue
                break
            }
        }
        if (status != Constant.MESSAGE_STATUS_READ) {
            status = CommonMethod.getIntFrom(message.status, user.key)
            if (status == -1) {
                status = Constant.MESSAGE_STATUS_SENT
            }
        }
        message.messageStatusCode = status
    }
}
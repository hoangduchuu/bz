package com.ping.android.domain.mapper

import com.ping.android.data.entity.CallEntity
import com.ping.android.model.Call
import com.ping.android.model.User
import com.ping.android.utils.configs.Constant
import java.util.*
import javax.inject.Inject

class CallMapper @Inject constructor() {
    fun transform(entity: CallEntity, user: User): Call {
        val opponentUserId = if (user.key == entity.senderId) entity.receiveId else entity.senderId
        val conversationID = if (user.key > opponentUserId) user.key + opponentUserId else opponentUserId + user.key
        val call = Call().apply {
            key = entity.key
            senderId = entity.senderId
            receiveId = entity.receiveId
            status = entity.status
            timestamp = entity.timestamp
            conversationId = conversationID

        }
        call.type = if (call.status == Constant.CALL_STATUS_SUCCESS) {
            if (call.senderId == user.key) {
                Call.CallType.OUTGOING
            } else {
                Call.CallType.INCOMING
            }
        } else {
            if (call.senderId == user.key) {
                Call.CallType.OUTGOING
            } else {
                Call.CallType.MISSED
            }
        }
        return call
    }

    fun transform(entities: List<CallEntity>, user: User): List<Call> {
        return entities.map {
            transform(it, user)
        }
    }

    fun reverseTransform(call: Call): CallEntity {
        val deleteStatuses = HashMap<String, Boolean>()
        deleteStatuses[call.senderId] = false
        deleteStatuses[call.receiveId] = false
        return CallEntity().apply {
            senderId = call.senderId
            receiveId = call.receiveId
            status = call.status
            timestamp = call.timestamp
            this.deleteStatuses = deleteStatuses
        }
    }
}
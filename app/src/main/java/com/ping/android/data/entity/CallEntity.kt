package com.ping.android.data.entity

import java.util.HashMap

class CallEntity {
    var key: String = ""
    var senderId: String = ""
    var receiveId: String = ""
    var status: Int = 0
    var timestamp: Double = 0.toDouble()
    var deleteStatuses: Map<String, Boolean> = HashMap()
}
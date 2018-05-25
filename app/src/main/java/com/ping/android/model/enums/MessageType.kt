package com.ping.android.model.enums

/**
 * Created by tuanluong on 2/28/18.
 */

enum class MessageType {
    UNKNOWN, TEXT, IMAGE, VOICE, GAME, VIDEO, CALL, MISSED_CALL;


    companion object {
        @JvmStatic
        fun from(messageType: Int): MessageType {
            return when (messageType) {
                2 -> IMAGE
                3 -> VOICE
                4 -> GAME
                5 -> VIDEO
                6 -> CALL
                7 -> MISSED_CALL
                else -> TEXT
            }
        }
    }
}

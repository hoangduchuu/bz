package com.ping.android.model.enums

import android.support.annotation.StringRes
import com.ping.android.R

enum class MessageCallType(val code: Int) {
    UNKNOWN(0),
    VOICE_CALL(1),
    VIDEO_CALL(2),
    MISSED_VOICE_CALL(3),
    MISSED_VIDEO_CALL(4);

    @StringRes
    fun descriptionFromMe(): Int {
        return when (this) {
            VIDEO_CALL -> R.string.chat_video_call_from_me
            VOICE_CALL -> R.string.chat_voice_call_from_me
            MISSED_VIDEO_CALL -> R.string.chat_missed_video_call_from_me
            MISSED_VOICE_CALL -> R.string.chat_missed_voice_call_from_me
            else -> 0
        }
    }

    @StringRes
    fun descriptionToMe(): Int {
        return when (this) {
            VIDEO_CALL -> R.string.chat_video_call_to_me
            VOICE_CALL -> R.string.chat_voice_call_to_me
            MISSED_VIDEO_CALL -> R.string.chat_missed_video_call_to_me
            MISSED_VOICE_CALL -> R.string.chat_missed_voice_call_to_me
            else -> 0
        }
    }

    companion object {
        @JvmStatic
        fun from(code: Int): MessageCallType {
            var type = MessageCallType.values().first { it.code == code }
            if (type == UNKNOWN) {
                type = VOICE_CALL
            }
            return type
        }
    }
}
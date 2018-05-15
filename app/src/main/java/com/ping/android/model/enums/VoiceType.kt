package com.ping.android.model.enums

enum class VoiceType {
    DEFAULT, CHIPMUNK, ROBOT, MALE, FEMALE;

    val filter: String
        get() {
            return when (this) {
                ROBOT -> "atempo=1/2,asetrate=44100*4/3"
                CHIPMUNK -> "asetrate=44100*3,atempo=0.75"
                FEMALE -> "atempo=3/4,asetrate=44100*4/3"
                MALE -> "asetrate=44100*3/4,atempo=4/3"
                else -> ""
            }
        }

    companion object {
        @JvmStatic
        fun from(voiceType: Int): VoiceType {
            return when (voiceType) {
                1 -> CHIPMUNK
                2 -> ROBOT
                3 -> MALE
                4 -> FEMALE
                else -> DEFAULT
            }
        }
    }
}
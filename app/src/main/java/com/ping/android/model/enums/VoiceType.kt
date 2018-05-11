package com.ping.android.model.enums

enum class VoiceType {
    DEFAULT, CHIPMUNK, FEMALE, MALE, ROBOT;

    val filter: String
        get() {
            return when (this) {
                ROBOT -> "atempo=1/2,asetrate=44100*4/3"
                CHIPMUNK -> "asetrate=44100*3,atempo=1"
                FEMALE -> "atempo=3/4,asetrate=44100*4/3"
                MALE -> "asetrate=44100*3/4,atempo=4/3"
                else -> ""
            }
        }
}
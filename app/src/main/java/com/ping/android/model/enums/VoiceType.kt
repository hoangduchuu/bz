package com.ping.android.model.enums

enum class VoiceType {
    CHIPMUNK, GIRL, MALE;

    val atempo: Float
        get() {
            return when (this) {
                CHIPMUNK -> 0.75f
                GIRL -> 1.2f
                MALE -> 1.5f
            }
        }
    val asetrate: Long
        get() {
            return when (this) {
                CHIPMUNK -> 20000
                GIRL -> 15000
                MALE -> 1000
            }
        }
}
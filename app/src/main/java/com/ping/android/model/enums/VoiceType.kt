package com.ping.android.model.enums

enum class VoiceType {
    DEFAULT, TELEPHONE, SONIC;
    //CHIPMUNK, ROBOT, MALE, FEMALE;

    val filter: String
        get() {
            return when (this) {
                TELEPHONE -> "lowpass=f=2000[lpf1];[lpf1]lowpass=f=2000[lpf2];[lpf2]highpass=f=500[hpf1];[hpf1]highpass=f=500[hpf2];[hpf2]acompressor,volume=2"
                SONIC -> "asetrate=44100*4"
//                ROBOT -> "atempo=1/2,asetrate=44100*4/3"
//                CHIPMUNK -> "asetrate=44100*3,atempo=0.75"
//                FEMALE -> "atempo=3/4,asetrate=44100*4/3"
//                MALE -> "asetrate=44100*3/4,atempo=4/3"
                else -> ""
            }
        }

    companion object {
        @JvmStatic
        fun from(voiceType: Int): VoiceType {
            return when (voiceType) {
                1 -> TELEPHONE
                2 -> SONIC
                else -> DEFAULT
            }
        }
    }
}
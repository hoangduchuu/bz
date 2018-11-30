package com.ping.android.utils

import com.orhanobut.logger.Logger

/**
 * this function is wrapper of `com.orhanobut.logger.Logger` library, which logging library help we can easy control our logcat 
 */
object BzLog{
    fun d(msg:String){
        Logger.t("").d(msg)
    }
}
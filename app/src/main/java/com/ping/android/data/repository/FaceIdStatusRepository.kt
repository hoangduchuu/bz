package com.ping.android.data.repository

import com.ping.android.utils.SharedPrefsHelper
import javax.inject.Inject

class FaceIdStatusRepository @Inject constructor()  {
    fun isFaceIdEnabled(): Boolean{
        return SharedPrefsHelper.getInstance().isFaceIdEnable
    }

    fun isFaceIdAvailable():Boolean{
        return SharedPrefsHelper.getInstance().isFaceIdCompleteTraining
    }

    fun disableFaceId(){
        SharedPrefsHelper.getInstance().isFaceIdEnable = false
    }

    fun enableFaceId(){
        SharedPrefsHelper.getInstance().isFaceIdEnable = true
    }

    fun markFaceIdIsTrainedSuccess(){
        SharedPrefsHelper.getInstance().isFaceIdCompleteTraining = true
    }
    fun markFaceIdIsNotTrained(){
        SharedPrefsHelper.getInstance().isFaceIdCompleteTraining = false
    }
}
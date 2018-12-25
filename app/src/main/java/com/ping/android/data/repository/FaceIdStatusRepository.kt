package com.ping.android.data.repository

import com.ping.android.utils.SharedPrefsHelper
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class FaceIdStatusRepository @Inject constructor()  {

    var faceIdRecognitionStatus: AtomicBoolean = AtomicBoolean(false)

    fun isFaceIdEnabled(): Boolean{
        return SharedPrefsHelper.getInstance().isFaceIdEnable
    }

    fun isFaceIdTrained():Boolean{
        return SharedPrefsHelper.getInstance().isFaceIdCompleteTraining
    }

    fun setFaceIdEnabled(value: Boolean){
        SharedPrefsHelper.getInstance().isFaceIdEnable = value
    }

    fun setFaceIdTrained(value: Boolean){
        SharedPrefsHelper.getInstance().isFaceIdCompleteTraining = value
    }
}
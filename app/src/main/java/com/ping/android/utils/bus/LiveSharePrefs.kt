package com.ping.android.utils.bus

import com.ping.android.utils.SharedPrefsHelper
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject

class LiveSharePrefs {
    companion object {
       private var ins: LiveSharePrefs? = null
        fun getInstance(): LiveSharePrefs?{
            if (ins == null){
                ins = LiveSharePrefs()
            }
            return ins
        }
    }
    val subject: PublishSubject<Boolean> = PublishSubject.create<Boolean>()
    val disposables = CompositeDisposable()

    fun changeFaceIDTrainingStatus(a : Boolean){
        SharedPrefsHelper.getInstance().isFaceIdCompleteTraining = a
        subject.onNext(a)
    }

    fun disableFaceID(){
        SharedPrefsHelper.getInstance().isFaceIdEnable = false
        subject.onNext(false)
    }

    fun registerListener(cosunmer: Consumer<Boolean> ){
        var d = subject.subscribe(cosunmer)
        disposables.add(d)
    }

    fun unregister(){
        disposables.clear()
    }
}
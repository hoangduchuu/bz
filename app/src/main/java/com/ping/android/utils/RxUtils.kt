package com.ping.android.utils

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

object RxUtils{
     fun countDown(seconds: Long):Observable<Long>{
       return Observable.timer(seconds, TimeUnit.SECONDS)
               .subscribeOn(Schedulers.computation())
               .observeOn(AndroidSchedulers.mainThread())

    }
}
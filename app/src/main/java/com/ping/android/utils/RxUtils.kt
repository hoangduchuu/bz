package com.ping.android.utils

import com.ping.android.BuildConfig
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

object RxUtils{
     fun countDown(seconds: Long):Observable<Long>{
       return Observable.interval(1, TimeUnit.SECONDS)
               .take(seconds)
               .map { t->t+1 }
               .subscribeOn(Schedulers.computation())
               .observeOn(AndroidSchedulers.mainThread())

    }
}
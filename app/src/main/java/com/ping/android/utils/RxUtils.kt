package com.ping.android.utils

import io.reactivex.Observable
import java.util.concurrent.TimeUnit

object RxUtils{
     fun countDownTenSeconds():Observable<Long>{
       return Observable.interval(1, TimeUnit.SECONDS)
               .take(10)
               .map { t->t+1 }

    }
}
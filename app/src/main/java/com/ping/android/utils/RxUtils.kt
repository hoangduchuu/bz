package com.ping.android.utils

import com.ping.android.BuildConfig
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

object RxUtils{
     fun countDownTenSeconds():Observable<Long>{
       return Observable.interval(1, TimeUnit.SECONDS)
               .take(BuildConfig.TIME_PROCESS_FACE_ID)
               .map { t->t+1 }

    }
}
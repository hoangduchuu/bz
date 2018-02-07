package com.bzzz.rxquickblox;

import com.quickblox.core.PerformProcessor;
import com.quickblox.core.server.Performer;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/6/18.
 */

public class RxJava2PerformProcessor implements PerformProcessor<Observable> {
    public static final PerformProcessor<?> INSTANCE = new RxJava2PerformProcessor();

    public RxJava2PerformProcessor() {
    }

    public <T> Observable<T> process(Performer<T> performer) {
        return Flowable.create(new PerformOnSubscribe(performer), BackpressureStrategy.MISSING)
                .toObservable();
    }
}

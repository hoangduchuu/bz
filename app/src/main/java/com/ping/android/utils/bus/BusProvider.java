package com.ping.android.utils.bus;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by tuanluong on 2/2/18.
 */
@Singleton
public class BusProvider {
    private final PublishSubject<Object> bus = PublishSubject.create();

    @Inject
    public BusProvider() {}

    public Observable<Object> getEvents() {
        return bus.share();
    }

    public void post(Object event) {
        bus.onNext(event);
    }
}

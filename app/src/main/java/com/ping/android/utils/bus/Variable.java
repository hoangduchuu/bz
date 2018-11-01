package com.ping.android.utils.bus;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class Variable<T> {
    private T value;

    private final BehaviorSubject<T> behaviorSubject = BehaviorSubject.create();

    public Variable(T value) {
        this.value = value;
        behaviorSubject.onNext(value);
    }

    public synchronized T get() {
        return value;
    }

    public synchronized void set(T value) {
        this.value = value;
        behaviorSubject.onNext(this.value);
    }

    public Observable<T> asObservable() {
        return behaviorSubject.share();
    }
}

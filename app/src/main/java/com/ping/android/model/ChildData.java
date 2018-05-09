package com.ping.android.model;

import com.bzzzchat.rxfirebase.database.ChildEvent;

/**
 * Created by tuanluong on 1/28/18.
 */

public class ChildData<T> {
    public T data;
    public ChildEvent.Type type;

    public ChildData() {}

    public ChildData(T data, ChildEvent.Type type) {
        this.data = data;
        this.type = type;
    }
}

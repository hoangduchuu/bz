package com.ping.android.model;

import com.bzzzchat.rxfirebase.events.ChildEvent;

/**
 * Created by tuanluong on 1/28/18.
 */

public class ChildData<T> {
    public T data;
    public ChildEvent.Type type;
}

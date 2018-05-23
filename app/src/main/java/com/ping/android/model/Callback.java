package com.ping.android.model;

public interface Callback {
    void complete(Object error, Object... data);
}

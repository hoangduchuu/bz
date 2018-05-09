package com.ping.android.domain;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.bzzzchat.cleanarchitecture.ThreadExecutor;

public class MainThreadExecutor implements ThreadExecutor {
    Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void execute(@NonNull Runnable command) {
        handler.post(command);
    }
}

package com.ping.android.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;

import java.lang.ref.WeakReference;

public class ActivityLifecycle implements Application.ActivityLifecycleCallbacks {
    private static final long CHECK_DELAY = 500;
    private static ActivityLifecycle instance;
    private WeakReference<Activity> foregroundActivity;

    private boolean foreground = false, paused = true;
    private Handler handler = new Handler();
    private Runnable check;

    private ActivityLifecycle() {
        foregroundActivity = new WeakReference<Activity>(null);
    }

    public static void init(Application app) {
        if (instance == null) {
            instance = new ActivityLifecycle();
            app.registerActivityLifecycleCallbacks(instance);
        }
    }

    public static synchronized ActivityLifecycle getInstance() {
        return instance;
    }

    public boolean isForeground() {
        return foreground;
    }

    public boolean isBackground() {
        return !foreground;
    }

    public Activity getForegroundActivity() {
        return foregroundActivity.get();
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
        foregroundActivity = new WeakReference<>(activity);
        paused = false;
        boolean wasBackground = !foreground;
        foreground = true;

        if (check != null)
            handler.removeCallbacks(check);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        paused = true;

        if (check != null)
            handler.removeCallbacks(check);

        handler.postDelayed(check = () -> {
            if (foreground && paused) {
                foreground = false;
            } else {
            }
        }, CHECK_DELAY);
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }
}

package com.ping.android.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public class ActivityLifecycle implements Application.ActivityLifecycleCallbacks {

    private static ActivityLifecycle instance;
    private static Activity foregroundActivity;

    private boolean foreground = false;

    private ActivityLifecycle() {
    }

    public static void init(Application app) {
        if (instance == null) {
            instance = new ActivityLifecycle();
            app.registerActivityLifecycleCallbacks(instance);
        }
    }

    public static Activity getForegroundActivity() {
        return foregroundActivity;
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

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        foregroundActivity = activity;
    }

    @Override
    public void onActivityStarted(Activity activity) {
        foregroundActivity = activity;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        foreground = true;
        foregroundActivity = activity;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        foreground = false;
        //foregroundActivity = activity;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        //foregroundActivity = activity;
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        foregroundActivity = activity;
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        //foregroundActivity = null;
    }
}

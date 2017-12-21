package com.ping.android;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.google.firebase.database.FirebaseDatabase;
import com.ping.android.utils.QBResRequestExecutor;
import com.ping.android.utils.ActivityLifecycle;

import java.io.File;

import okhttp3.Cache;
import okhttp3.OkHttpClient;

public class App extends CoreApp {

    private static App instance;
    private QBResRequestExecutor qbResRequestExecutor;

    public static App getInstance() {
        return instance;
    }
    private static Activity activeActivity;

    @Override
    public void onCreate() {
        super.onCreate();
        setupActivityListener();
        ActivityLifecycle.init(this);
        initApplication();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    private OkHttpClient createCacheClient(Context context){
        File httpCacheDirectory = new File(context.getCacheDir(), "bzzz");
        Cache cache = new Cache(httpCacheDirectory, 250000);

        return new OkHttpClient.Builder()
                .cache(cache)
                .build();
    }

    private void initApplication() {
        instance = this;
    }

    public synchronized QBResRequestExecutor getQbResRequestExecutor() {
        return qbResRequestExecutor == null
                ? qbResRequestExecutor = new QBResRequestExecutor()
                : qbResRequestExecutor;
    }

    private void setupActivityListener() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }
            @Override
            public void onActivityStarted(Activity activity) {
            }
            @Override
            public void onActivityResumed(Activity activity) {
                activeActivity = activity;
            }
            @Override
            public void onActivityPaused(Activity activity) {
                activeActivity = null;
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
        });
    }

    public static Activity getActiveActivity(){
        return activeActivity;
    }
}

package com.ping.android;

import android.content.Context;

import com.google.firebase.database.FirebaseDatabase;
import com.ping.android.util.QBResRequestExecutor;
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

    @Override
    public void onCreate() {
        super.onCreate();
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
}

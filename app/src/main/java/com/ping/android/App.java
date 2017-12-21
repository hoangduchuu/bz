package com.ping.android;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.ping.android.activity.BuildConfig;
import com.ping.android.activity.R;
import com.ping.android.util.QBResRequestExecutor;
import com.ping.android.utils.ActivityLifecycle;

import java.io.File;
import java.util.HashMap;

import okhttp3.Cache;
import okhttp3.OkHttpClient;

public class App extends CoreApp {

    private static App instance;
    private QBResRequestExecutor qbResRequestExecutor;
    private FirebaseRemoteConfig remoteConfig;

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
        remoteConfig = FirebaseRemoteConfig.getInstance();
        remoteConfig.setConfigSettings(new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(!BuildConfig.DEBUG).build());
        HashMap<String, Object> defaults = new HashMap<>();
        defaults.put("adr_store_current_version", "0.0.0");
        defaults.put("adr_force_update_enabled", 0);
        defaults.put("adr_app_id","com.ping.android");
        remoteConfig.setDefaults(defaults);
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

    private void fetchConfigSettings(){
        final Task<Void> fetch = remoteConfig.fetch(0);
        fetch.addOnCompleteListener(task -> {
            remoteConfig.activateFetched();
            forceUpdateCheck();
        });
    }

    private void forceUpdateCheck() {
        String storeCurrentVersion = remoteConfig.getString("adr_store_current_version");
        int forceUpdateEnabled = (int) remoteConfig.getLong("adr_force_update_enabled");
        String appId = remoteConfig.getString("adr_app_id");

        if(forceUpdateEnabled <= 0 || !versionCompare(storeCurrentVersion, BuildConfig.VERSION_NAME)){
            return;
        }

        new AlertDialog.Builder(activeActivity)
                .setTitle("Update Available")
                .setMessage(String.format("A new version of Bzzz is available on Google Play. Please update to version %s now.", storeCurrentVersion))
                .setCancelable(false)
                .setPositiveButton("Check it out!", (dialogInterface, i) -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("market://details?id=%s", appId)));
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        intent.setData(Uri.parse(String.format("https://play.google.com/store/apps/details?id=%s", appId)));
                        startActivity(intent);
                    }
                })
                .create()
                .show();
    }

    private boolean versionCompare(String storeVersion, String installedVersion){
        String[] ver1s = storeVersion.split("\\.");
        String[] ver2s = installedVersion.split("\\.");

        if (Integer.parseInt(ver1s[0]) > Integer.parseInt(ver2s[0])
                || Integer.parseInt(ver1s[1]) > Integer.parseInt(ver2s[1])){
            return true;
        }
        return false;
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
                fetchConfigSettings();
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

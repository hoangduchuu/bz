package com.ping.android;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.ping.android.dagger.ApplicationComponent;
import com.ping.android.dagger.ApplicationModule;
import com.ping.android.dagger.DaggerApplicationComponent;
import com.ping.android.utils.ActivityLifecycle;
import com.raizlabs.android.dbflow.config.FlowManager;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import io.fabric.sdk.android.Fabric;
import io.reactivex.plugins.RxJavaPlugins;
import nl.bravobit.ffmpeg.FFmpeg;

public class App extends CoreApp implements HasActivityInjector {

    private ApplicationComponent component;

    @Inject
    DispatchingAndroidInjector<Activity> dispatchingActivityInjector;

    @Override
    public void onCreate() {
        super.onCreate();
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            // This process is dedicated to LeakCanary for heap analysis.
//            // You should not init your app in this process.
//            return;
//        }
//        LeakCanary.install(this);
        getComponent().inject(this);
        ActivityLifecycle.init(this);
        FirebaseApp.initializeApp(getApplicationContext());
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        FirebaseMessaging.getInstance().subscribeToTopic("news")
                .addOnCompleteListener(task -> {
                    String msg = "Subscribe successfully";
                    if (!task.isSuccessful()) {
                        msg = "Subscribe unsuccessfully";
                    }
                    Log.d(TAG, msg);
                    if (BuildConfig.DEBUG) {
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
        // Set up Crashlytics, disabled for debug builds
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder()
                        .disabled(BuildConfig.DEBUG)
                        .build())
                .build();

        // Initialize Fabric with the debug-disabled crashlytics.
        Fabric.with(this, crashlyticsKit);
        setupRxErrorHandler();
        FlowManager.init(this);

        if (FFmpeg.getInstance(this).isSupported()) {
            // ffmpeg is supported
        } else {
            // ffmpeg is not supported
        }
    }

    public ApplicationComponent getComponent() {
        if (component == null) {
            component = DaggerApplicationComponent.builder()
                    .applicationModule(new ApplicationModule(this))
                    .build();
        }
        return component;
    }

    private void setupRxErrorHandler() {
        RxJavaPlugins.setErrorHandler(throwable -> throwable.printStackTrace());
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingActivityInjector;
    }
}

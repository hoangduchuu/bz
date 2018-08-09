package com.ping.android;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.ping.android.dagger.ApplicationComponent;
import com.ping.android.dagger.ApplicationModule;
import com.ping.android.dagger.DaggerApplicationComponent;
import com.ping.android.dagger.loggedin.LoggedInComponent;
import com.ping.android.dagger.loggedout.LoggedOutComponent;
import com.ping.android.utils.ActivityLifecycle;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.squareup.leakcanary.LeakCanary;

import io.fabric.sdk.android.Fabric;
import io.reactivex.plugins.RxJavaPlugins;
import nl.bravobit.ffmpeg.FFmpeg;

public class App extends CoreApp {

    private ApplicationComponent component;
    private LoggedInComponent loggedInComponent;
    private LoggedOutComponent loggedOutComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        ActivityLifecycle.init(this);
        FirebaseApp.initializeApp(getApplicationContext());
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
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

    public LoggedInComponent getLoggedInComponent() {
        if (loggedInComponent == null) {
            loggedInComponent = getComponent()
                    .provideLoggedInComponent();
        }
        return loggedInComponent;
    }

    public LoggedOutComponent getLoggedOutComponent() {
        if (loggedOutComponent == null) {
            loggedOutComponent = getComponent().provideLoggedOutComponent();
        }
        return loggedOutComponent;
    }

    private void setupRxErrorHandler() {
        RxJavaPlugins.setErrorHandler(throwable -> throwable.printStackTrace());
    }
}

package com.ping.android;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.ping.android.dagger.ApplicationComponent;
import com.ping.android.dagger.ApplicationModule;
import com.ping.android.dagger.DaggerApplicationComponent;
import com.ping.android.dagger.loggedin.LoggedInComponent;
import com.ping.android.dagger.loggedout.LoggedOutComponent;
import com.ping.android.utils.ActivityLifecycle;

public class App extends CoreApp {

    private ApplicationComponent component;
    private LoggedInComponent loggedInComponent;

    private LoggedOutComponent loggedOutComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        ActivityLifecycle.init(this);
        FirebaseApp.initializeApp(getApplicationContext());
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
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
}

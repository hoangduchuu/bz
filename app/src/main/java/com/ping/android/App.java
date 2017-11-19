package com.ping.android;

import com.google.firebase.database.FirebaseDatabase;
import com.ping.android.util.QBResRequestExecutor;
import com.ping.android.utils.ActivityLifecycle;
import com.ping.android.utils.FireBaseRequestHandler;
import com.squareup.picasso.Picasso;

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

        Picasso picasso = new Picasso.Builder(getApplicationContext())
                .addRequestHandler(new FireBaseRequestHandler())
                .build();
        Picasso.setSingletonInstance(picasso);
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

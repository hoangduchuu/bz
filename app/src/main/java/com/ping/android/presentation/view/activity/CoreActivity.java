package com.ping.android.presentation.view.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ping.android.App;
import com.ping.android.R;
import com.ping.android.dagger.ApplicationComponent;
import com.ping.android.dagger.loggedin.LoggedInComponent;
import com.ping.android.dagger.loggedout.LoggedOutComponent;
import com.ping.android.model.enums.NetworkStatus;
import com.ping.android.presentation.view.fragment.LoadingDialog;
import com.ping.android.service.CallService;
import com.ping.android.utils.SharedPrefsHelper;
import com.ping.android.utils.configs.Constant;
import com.ping.android.utils.NetworkConnectionChecker;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public abstract class CoreActivity extends AppCompatActivity implements NetworkConnectionChecker.OnConnectivityChangedListener {

    private NetworkConnectionChecker networkConnectionChecker;
    protected Map<DatabaseReference, Object> databaseReferences = new HashMap<>();
    // Disposable for UI events
    private CompositeDisposable disposables;
    public NetworkStatus networkStatus = NetworkStatus.CONNECTING;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disposables = new CompositeDisposable();
        initWiFiManagerListener();
        NetworkStatus networkStatus = networkConnectionChecker.getNetworkStatus();
        updateNetworkStatus(networkStatus);
    }

    @Override
    protected void onStart() {
        super.onStart();
        NetworkStatus networkStatus = networkConnectionChecker.getNetworkStatus();
        updateNetworkStatus(networkStatus);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getPresenter() != null) {
            getPresenter().resume();
        }
        networkConnectionChecker.registerListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (getPresenter() != null) {
            getPresenter().pause();
        }
        networkConnectionChecker.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getPresenter() != null) {
            getPresenter().destroy();
        }
        disposables.dispose();
        for (DatabaseReference reference : databaseReferences.keySet()) {
            Object listener = databaseReferences.get(reference);
            if (listener instanceof ChildEventListener) {
                reference.removeEventListener((ChildEventListener) listener);
            } else if (listener instanceof ValueEventListener) {
                reference.removeEventListener((ValueEventListener) listener);
            }
        }
    }

    @Override
    public void connectivityChanged(boolean availableNow) {

    }

    public boolean isNetworkAvailable() {
        return networkStatus == NetworkStatus.CONNECTED;
    }

    protected BasePresenter getPresenter() {
        return null;
    }

    protected ApplicationComponent getApplicationComponent() {
        return ((App) getApplication()).getComponent();
    }

    protected LoggedInComponent getLoggedInComponent() {
        return ((App) getApplication()).getLoggedInComponent();
    }

    protected LoggedOutComponent getLoggedOutComponent() {
        return ((App) getApplication()).getLoggedOutComponent();
    }

    protected void registerEvent(Disposable disposable) {
        disposables.add(disposable);
    }

    @Override
    public void connectivityChanged(NetworkStatus networkStatus) {
        updateNetworkStatus(networkStatus);
    }

    private void updateNetworkStatus(NetworkStatus networkStatus) {
        this.networkStatus = networkStatus;
        LinearLayout notifyNetworkLayout = findViewById(R.id.notify_network_layout);
        TextView notifyNetworkText = findViewById(R.id.notify_network_text);

        if (notifyNetworkLayout == null) return;

        switch (networkStatus) {
            case CONNECTED:
                notifyNetworkLayout.setVisibility(View.GONE);
                break;
            case CONNECTING:
                notifyNetworkLayout.setVisibility(View.VISIBLE);
                notifyNetworkLayout.setBackgroundResource(R.color.bg_network_connecting);
                notifyNetworkText.setText(getString(R.string.msg_network_connecting));
                break;
            case NOT_CONNECT:
                notifyNetworkLayout.setVisibility(View.VISIBLE);
                notifyNetworkLayout.setBackgroundResource(R.color.bg_network_noconnect);
                notifyNetworkText.setText(getString(R.string.no_internet_connection));
                break;
        }
    }

    private void initWiFiManagerListener() {
        networkConnectionChecker = new NetworkConnectionChecker(getApplication());
    }

    protected void exit() {
        finish();
    }

    DialogFragment loadingDialog;

    public void showLoading() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        loadingDialog = new LoadingDialog();
        loadingDialog.show(getSupportFragmentManager(), "LOADING");
    }

    public void hideLoading() {
        if (loadingDialog != null) {
            loadingDialog.dismissAllowingStateLoss();
        }
    }

    public void startCallService(Context context) {
        Integer qbId = SharedPrefsHelper.getInstance().get("quickbloxId");
        String pingId = SharedPrefsHelper.getInstance().get("pingId");
        CallService.start(context, qbId, pingId);
    }
}

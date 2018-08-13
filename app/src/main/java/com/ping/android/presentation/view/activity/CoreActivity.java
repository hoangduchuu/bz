package com.ping.android.presentation.view.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.ping.android.App;
import com.ping.android.R;
import com.ping.android.dagger.ApplicationComponent;
import com.ping.android.model.enums.NetworkStatus;
import com.ping.android.presentation.view.fragment.LoadingDialog;
import com.ping.android.service.CallService;
import com.ping.android.utils.NetworkConnectionChecker;
import com.ping.android.utils.SharedPrefsHelper;

import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public abstract class CoreActivity extends AppCompatActivity implements NetworkConnectionChecker.OnConnectivityChangedListener {

    private static NetworkConnectionChecker networkConnectionChecker;
    // Disposable for UI events
    private CompositeDisposable disposables;
    public NetworkStatus networkStatus = NetworkStatus.CONNECTING;
    private AtomicBoolean showLoading = new AtomicBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disposables = new CompositeDisposable();
        initWiFiManagerListener();
        //NetworkStatus networkStatus = networkConnectionChecker.getNetworkStatus();
        //updateNetworkStatus(networkStatus);
    }

    @Override
    protected void onStart() {
        super.onStart();
        NetworkStatus networkStatus = networkConnectionChecker.getNetworkStatus();
        if (networkStatus != this.networkStatus) {
            updateNetworkStatus(networkStatus);
        }
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
        if (showLoading.get()) {
            throw new IllegalStateException("Loading still showing");
        }
        //disposables.dispose();
        disposables.clear();
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

    public ApplicationComponent getApplicationComponent() {
        return ((App) getApplication()).getComponent();
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
        if (networkStatus == NetworkStatus.CONNECTED) {
            connectivityChanged(true);
        }
        LinearLayout notifyNetworkLayout = findViewById(R.id.notify_network_layout);
        TextView notifyNetworkText = findViewById(R.id.notify_network_text);

        if (notifyNetworkLayout == null) return;

        switch (networkStatus) {
            case CONNECTED:
                //TransitionManager.beginDelayedTransition(notifyNetworkLayout);
                notifyNetworkLayout.setVisibility(View.GONE);
                break;
            case CONNECTING:
                //TransitionManager.beginDelayedTransition(notifyNetworkLayout);
                notifyNetworkLayout.setVisibility(View.VISIBLE);
                notifyNetworkLayout.setBackgroundResource(R.color.bg_network_connecting);
                notifyNetworkText.setText(getString(R.string.msg_network_connecting));
                break;
            case NOT_CONNECT:
                //TransitionManager.beginDelayedTransition(notifyNetworkLayout, new Slide(Gravity.TOP));
                notifyNetworkLayout.setVisibility(View.VISIBLE);
                notifyNetworkLayout.setBackgroundResource(R.color.bg_network_noconnect);
                notifyNetworkText.setText(getString(R.string.no_internet_connection));
                break;
        }
    }

    private void initWiFiManagerListener() {
        if (networkConnectionChecker == null) {
            networkConnectionChecker = new NetworkConnectionChecker(getApplication());
        }
    }

    protected void exit() {
        finish();
    }

    DialogFragment loadingDialog;

    public void showLoading() {
        if (isFinishing() || isDestroyed() || showLoading.get()) return;

        loadingDialog = new LoadingDialog();
        loadingDialog.show(getSupportFragmentManager(), "LOADING");
    }

    public void hideLoading() {
        showLoading.set(false);
        if (loadingDialog != null) {
            loadingDialog.dismissAllowingStateLoss();
            loadingDialog = null;
        }
    }

    public void startCallService(Context context) {
        Integer qbId = SharedPrefsHelper.getInstance().get("quickbloxId");
        String pingId = SharedPrefsHelper.getInstance().get("pingId");
        CallService.start(context, qbId, pingId);
    }
}

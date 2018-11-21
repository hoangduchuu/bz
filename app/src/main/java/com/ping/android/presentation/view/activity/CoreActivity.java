package com.ping.android.presentation.view.activity;

import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public abstract class CoreActivity extends AppCompatActivity implements NetworkConnectionChecker.OnConnectivityChangedListener {
    private CompositeDisposable disposables;
    public NetworkStatus networkStatus = NetworkStatus.CONNECTING;
    private AtomicBoolean showLoading = new AtomicBoolean(false);
    private Handler netWorkHandler = new Handler();
    AlertDialog.Builder dialogBuilder;
    AlertDialog dialog;

    @Inject
    NetworkConnectionChecker networkConnectionChecker;

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
        if (getPresenter() != null) {
            getPresenter().destroy();
        }
        if (showLoading.get()) {
            //throw new IllegalStateException("Loading still showing");
            hideLoading();
        }
        //disposables.dispose();
        disposables.clear();
        super.onDestroy();
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
                notifyNetworkLayout.setBackgroundResource(R.color.bg_network_connecting);
                notifyNetworkText.setText(getString(R.string.connecting));
               netWorkHandler.postDelayed(new Runnable() {
                   @Override
                   public void run() {
                       notifyNetworkLayout.setVisibility(View.VISIBLE);
                       notifyNetworkLayout.setBackgroundResource(R.color.bg_network_noconnect);
                       notifyNetworkText.setText(getString(R.string.no_internet_connection));
                   }
               },2000);
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
        if (showLoading.get() || isFinishing() || isDestroyed()) return;
        showLoading.set(true);
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

    // HUU
    /**
     * show alert dialogBuilder
     */
    public void showConfirmMessageDialog(String title, String message){
        if (dialogBuilder == null){
            dialogBuilder = new AlertDialog.Builder(this);
        }
        dialogBuilder.setTitle(title).setMessage(message)
                .setPositiveButton(getString(R.string.core_ok), (dialog, which) -> dialog.dismiss());
        if (dialog == null){
            dialog = dialogBuilder.create();
        }
        new Handler().postDelayed(() -> {
            dialog.show();
            makePositiveButtonCenter();
        },200);
    }

    void makePositiveButtonCenter() {
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        LinearLayout parent = (LinearLayout) positiveButton.getParent();
        positiveButton.setLayoutParams(new android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.MATCH_PARENT));
        parent.setGravity(Gravity.CENTER_HORIZONTAL);
        View leftSpacer = parent.getChildAt(1);
        leftSpacer.setVisibility(View.GONE);
    }
}

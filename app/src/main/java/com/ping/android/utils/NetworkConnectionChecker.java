package com.ping.android.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.ping.android.model.enums.NetworkStatus;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.inject.Inject;

public class NetworkConnectionChecker {

    private final ConnectivityManager connectivityManager;

    private Set<OnConnectivityChangedListener> listeners = new CopyOnWriteArraySet<>();
    private boolean isConnectedNow;

    @Inject
    public NetworkConnectionChecker(Context context) {
        this.connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(new NetworkStateReceiver(), intentFilter);
    }

    public void registerListener(OnConnectivityChangedListener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(OnConnectivityChangedListener listener) {
        listeners.remove(listener);
    }

    public boolean isConnected() {
        return isConnectedNow;
    }

    public boolean isConnectedNow() {
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public NetworkStatus getNetworkStatus() {
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting()){
            if(activeNetworkInfo.isConnected())
                return NetworkStatus.CONNECTED;
            return NetworkStatus.CONNECTING;
        }
        return NetworkStatus.NOT_CONNECT;
    }

    public interface OnConnectivityChangedListener {
        void connectivityChanged(boolean availableNow);
        void connectivityChanged(NetworkStatus networkStatus);
    }

    private class NetworkStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            isConnectedNow = isConnectedNow();

            for (OnConnectivityChangedListener listener : listeners) {
                listener.connectivityChanged(isConnectedNow);
                listener.connectivityChanged(getNetworkStatus());
            }
        }
    }
}

package com.ping.android.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ping.android.ultility.Constant;
import com.ping.android.util.NetworkConnectionChecker;

public class CoreActivity extends AppCompatActivity implements NetworkConnectionChecker.OnConnectivityChangedListener {

    private NetworkConnectionChecker networkConnectionChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWiFiManagerListener();
        Constant.NETWORK_STATUS networkStatus = networkConnectionChecker.getNetworkStatus();
        UpdateNetworkStatus(networkStatus);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Constant.NETWORK_STATUS networkStatus = networkConnectionChecker.getNetworkStatus();
        UpdateNetworkStatus(networkStatus);
    }

    @Override
    protected void onResume() {
        super.onResume();
        networkConnectionChecker.registerListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        networkConnectionChecker.unregisterListener(this);
    }

    @Override
    public void connectivityChanged(boolean availableNow) {

    }

    @Override
    public void connectivityChanged(Constant.NETWORK_STATUS networkStatus) {
        UpdateNetworkStatus(networkStatus);
    }

    private void UpdateNetworkStatus(Constant.NETWORK_STATUS networkStatus)
    {
        LinearLayout notifyNetworkLayout = (LinearLayout) findViewById(R.id.notify_network_layout);
        TextView notifyNetworkText = (TextView) findViewById(R.id.notify_network_text);

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
            case NOCONNECT:
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
}

package com.ping.android.device;

/**
 * Created by tuanluong on 2/6/18.
 */

public interface Device {
    String getDeviceId();

    boolean getNetworkStatus();

    String getExternalImageFolder();

    void refreshMedia(String file);
}

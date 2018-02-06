package com.ping.android.device.impl;

import android.app.Application;
import android.provider.Settings;

import com.ping.android.device.Device;

import javax.inject.Inject;

/**
 * Created by tuanluong on 2/6/18.
 */

public class DeviceImpl implements Device {
    @Inject
    Application application;

    @Inject
    public DeviceImpl() {}

    @Override
    public String getDeviceId() {
        return Settings.Secure.getString(application.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }
}

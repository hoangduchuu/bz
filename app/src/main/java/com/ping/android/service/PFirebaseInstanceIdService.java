package com.ping.android.service;

import com.google.firebase.iid.FirebaseInstanceIdService;
import com.quickblox.messages.services.SubscribeService;

/**
 * Created by Tung Tran on 12/25/2017.
 */

public class PFirebaseInstanceIdService extends FirebaseInstanceIdService {
    private static final String TAG = PFirebaseInstanceIdService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        SubscribeService.subscribeToPushes(getApplicationContext(), true);
    }
}

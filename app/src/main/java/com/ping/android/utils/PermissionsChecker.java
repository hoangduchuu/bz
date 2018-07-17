package com.ping.android.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;

public class PermissionsChecker {
    private Context context;

    private Activity activity;
    private Fragment fragment;

    private PublishSubject<Boolean> publishSubject = PublishSubject.create();

    private PermissionsChecker(Activity activity) {
        this.activity = activity;
        this.context = activity;
    }

    private PermissionsChecker(Fragment fragment) {
        this.fragment = fragment;
    }

    public static PermissionsChecker from(Activity activity) {
        return new PermissionsChecker(activity);
    }

    public static PermissionsChecker from(Fragment fragment) {
        return new PermissionsChecker(fragment);
    }

    public boolean lacksPermissions(String... permissions) {
        for (String permission : permissions) {
            if (lacksPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    public boolean lacksPermission(String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED;
    }

    public Single<Boolean> check(String... permissions) {
        if (lacksPermissions(permissions)) {
            requestPermissions(permissions);
            return publishSubject.share()
                    .take(1)
                    .single(false);
        }
        return Single.just(true);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissions(String... permissions) {
        if (activity != null) {
            activity.requestPermissions(permissions, 123);
        } else if (fragment != null) {
            fragment.requestPermissions(permissions, 123);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 123: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    boolean isGrant = true;
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            isGrant = false;
                            break;
                        }
                    }
                    boolean finalIsGrant = isGrant;
                    new Handler().postDelayed(() -> publishSubject.onNext(finalIsGrant), 700);
                } else {
                    publishSubject.onNext(false);
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    // TODO show error
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
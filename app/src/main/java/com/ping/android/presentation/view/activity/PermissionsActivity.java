package com.ping.android.presentation.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import com.ping.android.R;
import com.ping.android.utils.PermissionsChecker;
import com.ping.android.utils.Toaster;

public class PermissionsActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 0;
    private static final String EXTRA_PERMISSIONS = "extraPermissions";
    private static final String CHECK_ONLY_AUDIO = "checkAudio";
    private PermissionsChecker checker;
    private boolean requiresCheck;

    public static void startActivity(Activity activity, boolean checkOnlyAudio, int code, String... permissions) {
        Intent intent = new Intent(activity, PermissionsActivity.class);
        intent.putExtra(EXTRA_PERMISSIONS, permissions);
        intent.putExtra(CHECK_ONLY_AUDIO, checkOnlyAudio);
        ActivityCompat.startActivityForResult(activity, intent, code, null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() == null || !getIntent().hasExtra(EXTRA_PERMISSIONS)) {
            throw new RuntimeException("This Activity needs to be launched using the static startActivityForResult() method.");
        }
        setContentView(R.layout.activity_permissions);

        checker = PermissionsChecker.from(this);
        checkPermissions();
        //requiresCheck = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (requiresCheck) {
//            checkPermissions();
//        } else {
//            requiresCheck = true;
//        }
    }

    private void checkPermissions() {
        String[] permissions = getPermissions();
        boolean checkOnlyAudio = getCheckOnlyAudio();

        if (checkOnlyAudio) {
            checkPermissionAudio(permissions[1]);
        } else {
            checkPermissionAudioVideo(permissions);
        }
    }

    private void checkPermissionAudio(String audioPermission) {
        if (checker.lacksPermissions(audioPermission)) {
            requestPermissions(audioPermission);
        } else {
            allPermissionsGranted();
        }
    }

    private void allPermissionsGranted() {
        setResult(RESULT_OK);
        finish();
    }

    private void checkPermissionAudioVideo(String[] permissions) {
        if (checker.lacksPermissions(permissions)) {
            requestPermissions(permissions);
        } else {
            allPermissionsGranted();
        }
    }

    private String[] getPermissions() {
        return getIntent().getStringArrayExtra(EXTRA_PERMISSIONS);
    }

    private boolean getCheckOnlyAudio() {
        return getIntent().getBooleanExtra(CHECK_ONLY_AUDIO, false);
    }

    private void requestPermissions(String... permissions) {
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && hasAllPermissionsGranted(grantResults)) {
            requiresCheck = true;
            allPermissionsGranted();
        } else {
            requiresCheck = false;
            showDeniedResponse(grantResults);
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private void showDeniedResponse(int[] grantResults) {
        if (grantResults.length > 1) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != 0) {
                    Toaster.longToast(getString(R.string.permission_unavailable, permissionFeatures.values()[i]));
                }
            }
        } else {
            Toaster.longToast(getString(R.string.permission_unavailable, permissionFeatures.MICROPHONE));
        }
    }

    private boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    private enum permissionFeatures {
        CAMERA,
        MICROPHONE
    }
}
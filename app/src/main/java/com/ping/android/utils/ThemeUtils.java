package com.ping.android.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.view.Window;
import android.view.WindowManager;

import com.ping.android.model.enums.Color;

public class ThemeUtils {
    public static Color currentColor = Color.COLOR_6;

    public static void changeToTheme(Activity activity, Bundle extras) {
        activity.finish();
        Intent intent = new Intent(activity, activity.getClass());
        intent.putExtras(extras);
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
    }

    public static void onActivityCreateSetTheme(Activity activity, Color color) {
        activity.setTheme(color.getTheme());
        final Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(activity, color.statusBarColor()));
    }
}

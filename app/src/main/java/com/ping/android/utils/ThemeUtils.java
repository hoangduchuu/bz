package com.ping.android.utils;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.view.Window;
import android.view.WindowManager;

import com.ping.android.model.enums.Color;

public class ThemeUtils {
    public static Color currentColor = Color.COLOR_6;

    public static void changeToTheme(Activity activity, Color color) {
        currentColor = color;
        activity.finish();
        Intent intent = new Intent(activity, activity.getClass());
        intent.putExtras(activity.getIntent());
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
    }

    public static void onActivityCreateSetTheme(Activity activity) {
        activity.setTheme(currentColor.getTheme());
        final Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(activity, currentColor.statusBarColor()));
    }
}

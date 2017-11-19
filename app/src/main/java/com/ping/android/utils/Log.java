package com.ping.android.utils;

import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.ping.android.activity.BuildConfig;

/**
 * Wrapper class for Log.{d,w,e}
 */
public class Log {
    public static String TAG = "android";
    public static boolean isDebug = BuildConfig.DEBUG;

    public static void d() {
        if (isDebug) {
            StackTraceElement t = Thread.currentThread().getStackTrace()[3];
            android.util.Log.d(
                    TAG,
                    t.getClassName().substring(
                            t.getClassName().lastIndexOf('.') + 1)
                            + "." + t.getMethodName());
        }
    }

    public static void d(String msg) {
        if (isDebug) {
            StackTraceElement t = Thread.currentThread().getStackTrace()[3];
            android.util.Log.d(
                    TAG,
                    t.getClassName().substring(
                            t.getClassName().lastIndexOf('.') + 1)
                            + "." + t.getMethodName() + " " + msg);
        }
    }

    public static void d(String msg, Throwable tr) {
        if (isDebug)
            android.util.Log.d(TAG, msg, tr);
    }

    public static void w(Throwable tr) {
        if (isDebug)
            android.util.Log.w(TAG, tr.getLocalizedMessage(), tr);
    }

    public static void w(String msg) {
        if (isDebug)
            android.util.Log.w(TAG, msg);
    }

    public static void w(String msg, Throwable tr) {
        if (isDebug)
            android.util.Log.w(TAG, msg, tr);
    }

    public static void e(Throwable tr) {
        if (isDebug) {
            android.util.Log.e(TAG, tr.getLocalizedMessage(), tr);
        } else if (!BuildConfig.DEBUG){
            Crashlytics.logException(tr);
        }
    }

    public static void e(String msg) {
        if (isDebug && !TextUtils.isEmpty(msg))
            android.util.Log.e(TAG, msg);
    }

    public static void e(String msg, Throwable tr) {
        if (isDebug)
            android.util.Log.e(TAG, msg, tr);
    }

    public static void strictMode() {
        /*if (isDebug) {
			if (android.os.Build.VERSION.SDK_INT >= 9) {
				StrictMode
						.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
								.detectAll().penaltyLog().build());
				StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
						.detectAll().penaltyLog().build());
			}
		}*/
    }

    public static void i(String logtag, String string) {
        // TODO Auto-generated method stub
        if (isDebug)
            android.util.Log.i(logtag, string);
    }

    public static void i(String msg) {
        if (isDebug)
            android.util.Log.i(TAG, msg);
    }
}
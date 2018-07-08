package com.ping.android.utils;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.PopupWindow;

public class Utils {

    static final int DONT_UPDATE_FLAG = -1;

    @NonNull
    public static Rect windowVisibleDisplayFrame(@NonNull final Activity context) {
        final Rect result = new Rect();
        context.getWindow().getDecorView().getWindowVisibleDisplayFrame(result);
        return result;
    }

    public static int screenHeight(@NonNull final Activity context) {
        final Point size = new Point();

        context.getWindowManager().getDefaultDisplay().getSize(size);

        return size.y;
    }

    @NonNull
    public static Point locationOnScreen(@NonNull final View view) {
        final int[] location = new int[2];
        view.getLocationOnScreen(location);
        return new Point(location[0], location[1]);
    }

    public static void fixPopupLocation(@NonNull final PopupWindow popupWindow, @NonNull final Point desiredLocation) {
        popupWindow.getContentView().post(new Runnable() {
            @Override public void run() {
                final Point actualLocation = locationOnScreen(popupWindow.getContentView());

                if (!(actualLocation.x == desiredLocation.x && actualLocation.y == desiredLocation.y)) {
                    final int differenceX = actualLocation.x - desiredLocation.x;
                    final int differenceY = actualLocation.y - desiredLocation.y;

                    final int fixedOffsetX;
                    final int fixedOffsetY;

                    if (actualLocation.x > desiredLocation.x) {
                        fixedOffsetX = desiredLocation.x - differenceX;
                    } else {
                        fixedOffsetX = desiredLocation.x + differenceX;
                    }

                    if (actualLocation.y > desiredLocation.y) {
                        fixedOffsetY = desiredLocation.y - differenceY;
                    } else {
                        fixedOffsetY = desiredLocation.y + differenceY;
                    }

                    popupWindow.update(fixedOffsetX, fixedOffsetY, DONT_UPDATE_FLAG, DONT_UPDATE_FLAG);
                }
            }
        });
    }
}

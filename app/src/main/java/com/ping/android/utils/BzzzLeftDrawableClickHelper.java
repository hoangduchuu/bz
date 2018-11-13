package com.ping.android.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.widget.EditText;

import com.ping.android.R;

import java.util.Objects;

public class BzzzLeftDrawableClickHelper {
    @SuppressLint("ClickableViewAccessibility")
    public static void showHideButton(Context context, EditText txtRetypePassword,Boolean isHaveLefIcon) {
        final String[] dName = new String[1];
        Drawable eyeOpendDrawableRight, leftDrwablePassword;
        dName[0] =context. getResources().getResourceEntryName(R.drawable.ic_eye_closed);
        eyeOpendDrawableRight = context.getResources().getDrawable(R.drawable.ic_eye_open);
        eyeOpendDrawableRight.setTint(context.getColor(R.color.yellow_eye));

        leftDrwablePassword = context.getResources().getDrawable(R.drawable.ic_login_password);

        txtRetypePassword.setOnTouchListener((v, event) -> {
            final int DRAWABLE_LEFT = 0;
            final int DRAWABLE_TOP = 1;
            final int DRAWABLE_RIGHT = 2;
            final int DRAWABLE_BOTTOM = 3;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (txtRetypePassword.getCompoundDrawables()[DRAWABLE_RIGHT] == null) {
                    return false;
                }
                if (event.getRawX() >= (txtRetypePassword.getRight() - txtRetypePassword.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    Drawable d = txtRetypePassword.getCompoundDrawables()[DRAWABLE_RIGHT];
                    if (Objects.equals(dName[0],context. getResources().getResourceEntryName(R.drawable.ic_eye_closed))) {
                        if (isHaveLefIcon) {
                            txtRetypePassword.setCompoundDrawablesWithIntrinsicBounds(leftDrwablePassword, null, eyeOpendDrawableRight, null);

                        } else {
                            txtRetypePassword.setCompoundDrawablesWithIntrinsicBounds(null, null, eyeOpendDrawableRight, null);

                        }
                        dName[0] = context.getResources().getResourceEntryName(R.drawable.ic_eye_open);
                        txtRetypePassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    } else {
                        if (isHaveLefIcon) {
                            txtRetypePassword.setCompoundDrawablesWithIntrinsicBounds(leftDrwablePassword, null,context. getDrawable(R.drawable.ic_eye_closed), null);

                        }else {
                            txtRetypePassword.setCompoundDrawablesWithIntrinsicBounds(null, null,context. getDrawable(R.drawable.ic_eye_closed), null);

                        }
                        txtRetypePassword.setTransformationMethod(PasswordTransformationMethod.getInstance());

                        dName[0] = context.getResources().getResourceEntryName(R.drawable.ic_eye_closed);

                    }
                    return true;
                }
            }
            return false;
        });
    }

}

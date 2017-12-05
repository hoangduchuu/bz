package com.ping.android.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ping.android.CoreApp;
import com.ping.android.activity.R;
import com.ping.android.model.User;
import com.ping.android.ultility.Callback;

import org.jivesoftware.smack.util.StringUtils;

import java.io.File;
import java.util.Random;

public class UiUtils {

    public static final int IMG_DEFAULT = R.drawable.ic_avatar_gray;

    private static final int RANDOM_COLOR_START_RANGE = 0;
    private static final int RANDOM_COLOR_END_RANGE = 9;

    private static final Random random = new Random();
    private static int previousColor;

    private UiUtils() {
    }

    public static Drawable getGreyCircleDrawable() {
        return getColoredCircleDrawable(ResourceUtils.getColor(R.color.color_grey));
    }

    public static Drawable getRandomColorCircleDrawable() {
        return getColoredCircleDrawable(getRandomCircleColor());
    }

    public static Drawable getColorCircleDrawable(int colorPosition) {
        return getColoredCircleDrawable(getCircleColor(colorPosition % RANDOM_COLOR_END_RANGE));
    }

    public static Drawable getColoredCircleDrawable(@ColorInt int color) {
        GradientDrawable drawable = (GradientDrawable) ResourceUtils.getDrawable(R.drawable.shape_circle);
        drawable.setColor(color);
        return drawable;
    }

    public static int getRandomCircleColor() {
        int randomNumber = random.nextInt(RANDOM_COLOR_END_RANGE) + 1;

        int generatedColor = getCircleColor(randomNumber);
        if (generatedColor != previousColor) {
            previousColor = generatedColor;
            return generatedColor;
        } else {
            do {
                generatedColor = getRandomCircleColor();
            } while (generatedColor != previousColor);
        }
        return previousColor;
    }

    public static int getCircleColor(@IntRange(from = RANDOM_COLOR_START_RANGE, to = RANDOM_COLOR_END_RANGE)
                                             int colorPosition) {
        String colorIdName = String.format("random_color_%d", colorPosition + 1);
        int colorId = CoreApp.getInstance().getResources()
                .getIdentifier(colorIdName, "color", CoreApp.getInstance().getPackageName());

        return ResourceUtils.getColor(colorId);
    }

    public static void displayProfileImage(final Context context, final ImageView imageView, User user) {
        displayProfileImage(context, imageView, user, false);
    }

    public static void displayProfileImage(final Context context, final ImageView imageView, User user, boolean currentProfile) {

        if (context == null || imageView == null) {
            return;
        }
        boolean showProfile = true;
        if (user != null && user.settings != null && user.settings.private_profile) {
            showProfile = false;
        }
        if (user != null && StringUtils.isNotEmpty(user.profile) && (showProfile || currentProfile)) {

            Log.d(user.profile);

            StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(user.profile);
            GlideApp.with(imageView.getContext())
                    .load(gsReference)
                    .error(R.drawable.ic_avatar_gray)
                    .apply(RequestOptions.circleCropTransform())
                    .into(imageView);
        } else {
            imageView.setImageResource(IMG_DEFAULT);
        }
    }

    public static void displayProfileAvatar(ImageView imageView, File filePath) {
        if (filePath == null) {
            imageView.setImageResource(IMG_DEFAULT);
            return;
        }
        GlideApp.with(imageView.getContext())
                .load(filePath)
                .apply(RequestOptions.circleCropTransform())
                .into(imageView);
    }

    public static void displayProfileAvatar(ImageView imageView, String firebaseUrl) {
        if (TextUtils.isEmpty(firebaseUrl)) {
            imageView.setImageResource(IMG_DEFAULT);
            return;
        }
        StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(firebaseUrl);
        GlideApp.with(imageView.getContext())
                .load(gsReference)
                .apply(RequestOptions.circleCropTransform())
                .into(imageView);
    }

    public static SimpleTarget<Bitmap> loadImage(ImageView imageView, String imageUrl, Callback callback) {
        if (TextUtils.isEmpty(imageUrl)) {
            return null;
        }
        StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
        SimpleTarget<Bitmap> target = new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                Log.d("Image loaded " + imageUrl);
                callback.complete(null, resource);
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                callback.complete(new Error());
            }
        };
        GlideApp.with(imageView.getContext())
                .asBitmap()
                .load(gsReference)
                .placeholder(R.drawable.img_loading_bottom)
                .override(512)
                .into(target);
        return target;
    }

    public static void hideSoftKeyboard(Activity activity) {
        if (activity == null || activity.getWindow() == null || activity.getWindow().getDecorView() == null
                || activity.getWindow().getDecorView().getWindowToken() == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
    }

    public static void setUpHideSoftKeyboard(final Activity activity, final View view) {
        //Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(activity);
                    return false;
                }
            });
        }

        if (view instanceof ViewGroup) { //If a layout container, iterate over children and seed recursion.
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setUpHideSoftKeyboard(activity, innerView);
            }
        }
    }
}

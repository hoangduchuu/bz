package com.ping.android.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntRange;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.bzzzchat.configuration.GlideApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ping.android.CoreApp;
import com.ping.android.R;
import com.ping.android.model.Callback;
import com.ping.android.model.User;

import org.jivesoftware.smack.util.StringUtils;

import java.io.File;
import java.util.Locale;
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
        String colorIdName = String.format(Locale.getDefault(), "random_color_%d", colorPosition + 1);
        int colorId = CoreApp.getInstance().getResources()
                .getIdentifier(colorIdName, "color", CoreApp.getInstance().getPackageName());

        return ResourceUtils.getColor(colorId);
    }

    public static void displayProfileImage(final Context context, final ImageView imageView, User user) {
        displayProfileImage(context, imageView, user, R.drawable.ic_avatar_gray, false);
    }

    public static void displayProfileImage(final Context context, final ImageView imageView, User user, @DrawableRes int placeholder, boolean currentProfile) {

        if (context == null || imageView == null) {
            return;
        }
        boolean showProfile = true;
        if (user != null && user.settings != null && user.settings.private_profile) {
            showProfile = false;
        }
        if (user != null && StringUtils.isNotEmpty(user.profile) && (showProfile || currentProfile)) {

            //Log.d(user.profile);

            StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(user.profile);
            GlideApp.with(imageView.getContext())
                    .load(gsReference)
                    .profileImage()
                    .into(imageView);
        } else {
            imageView.setImageResource(placeholder);
        }
    }

    public static void displayProfileImage(final ImageView imageView, User user, Callback callback) {
        boolean showProfile = true;
        if (user != null && user.settings != null && user.settings.private_profile) {
            showProfile = false;
        }
        if (user != null && StringUtils.isNotEmpty(user.profile) && showProfile) {
            Log.d(user.profile);

            StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(user.profile);
            GlideApp.with(imageView.getContext())
                    .load(gsReference)
                    .placeholder(R.drawable.ic_avatar_gray)
                    .error(R.drawable.ic_avatar_gray)
                    .apply(RequestOptions.circleCropTransform())
                    .override(200, 200)
                    .dontAnimate()
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            if (callback != null) {
                                callback.complete(null);
                            }
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            if (callback != null) {
                                callback.complete(null);
                            }
                            return false;
                        }
                    })
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
                .override(200, 200)
                .apply(RequestOptions.circleCropTransform())
                .into(imageView);
    }

    public static void displayProfileAvatar(ImageView imageView, String firebaseUrl) {
        if (TextUtils.isEmpty(firebaseUrl) || !firebaseUrl.startsWith("gs://")) {
            imageView.setImageResource(IMG_DEFAULT);
            return;
        }
        StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(firebaseUrl);
        GlideApp.with(imageView.getContext())
                .load(gsReference)
                .placeholder(R.drawable.ic_avatar_gray)
                .error(R.drawable.ic_avatar_gray)
                .override(100, 100)
                .apply(RequestOptions.circleCropTransform())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }

    public static void displayProfileAvatar(ImageView imageView, String firebaseUrl, @DrawableRes int placeholder) {
        if (TextUtils.isEmpty(firebaseUrl) || !firebaseUrl.startsWith("gs://")) {
            imageView.setImageResource(placeholder);
            return;
        }
        StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(firebaseUrl);
        GlideApp.with(imageView.getContext())
                .load(gsReference)
                .placeholder(placeholder)
                .error(placeholder)
                .override(100, 100)
                .apply(RequestOptions.circleCropTransform())
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(imageView);
    }

    public static void displayProfileAvatar(Fragment fragment, ImageView imageView, String firebaseUrl, Callback callback) {
        if (TextUtils.isEmpty(firebaseUrl)) {
            imageView.setImageResource(IMG_DEFAULT);
            if (callback != null) {
                callback.complete(null);
            }
            return;
        }
        StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(firebaseUrl);
        GlideApp.with(fragment)
                .load(gsReference)
                .apply(RequestOptions.circleCropTransform())
                .override(100, 100)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .dontAnimate()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        callback.complete(e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        callback.complete(null);
                        return false;
                    }
                })
                .into(imageView);
    }

    public static void loadImageFromFile(ImageView imageView, String filePath, String messageKey, boolean bitmapMark) {
        ObjectKey key = new ObjectKey(String.format("%s%s", messageKey, bitmapMark ? "encoded" : "decoded"));
        Drawable placeholder = ContextCompat.getDrawable(imageView.getContext(), R.drawable.img_loading_image);
        GlideApp.with(imageView.getContext())
                .load(filePath)
                .placeholder(placeholder)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .override(512)
                .transform(new BitmapEncode(bitmapMark))
                .signature(key)
                .dontAnimate()
                .into(imageView);
    }

    public static void loadImageFromFile(ImageView imageView, String filePath, String messageKey, boolean bitmapMark, Callback callback) {
        SimpleTarget<Bitmap> target = new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                Log.d("Image loaded " + filePath);
                callback.complete(null, resource);
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                callback.complete(new Error());
            }
        };
        ObjectKey key = new ObjectKey(String.format("%s%s", messageKey, bitmapMark ? "encoded" : "decoded"));
        GlideApp.with(imageView.getContext())
                .asBitmap()
                .load(filePath)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(512)
                .transform(new BitmapEncode(bitmapMark))
                .signature(key)
                .dontAnimate()
                .into(target);
    }

    public static void loadImage(ImageView imageView, String imageUrl, String messageKey, boolean bitmapMark, Drawable placeholder) {
        if (TextUtils.isEmpty(imageUrl) || !imageUrl.startsWith("gs")) {
            return;
        }
        if (placeholder == null) {
            placeholder = ContextCompat.getDrawable(imageView.getContext(), R.drawable.img_loading_image);
        }

        StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
        ObjectKey key = new ObjectKey(String.format("%s%s", messageKey, bitmapMark ? "encoded" : "decoded"));
        GlideApp.with(imageView.getContext())
                .load(gsReference)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(placeholder)
                .override(512)
                .transform(new BitmapEncode(bitmapMark))
                .signature(key)
                .dontAnimate()
                .into(imageView);
    }

    public static SimpleTarget<Bitmap> loadImage(ImageView imageView, String imageUrl, String messageKey, boolean bitmapMark, Callback callback) {
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
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .load(gsReference)
                .placeholder(R.drawable.img_loading_bottom)
                .override(512)
                .transform(new BitmapEncode(bitmapMark))
                .signature(new ObjectKey(String.format("%s%s", messageKey, bitmapMark ? "encoded" : "decoded")))
                .into(target);
        return target;
    }

    public static SimpleTarget<Bitmap> loadImage(ImageView imageView, String imageUrl, String messageKey, boolean bitmapMark, Drawable placeholder, Callback callback) {
        if (TextUtils.isEmpty(imageUrl)) {
            return null;
        }
        if (placeholder == null) {
            placeholder = ContextCompat.getDrawable(imageView.getContext(), R.drawable.img_loading_image);
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
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .load(gsReference)
                .placeholder(placeholder)
                .override(512)
                .transform(new BitmapEncode(bitmapMark))
                .signature(new ObjectKey(String.format("%s%s", messageKey, bitmapMark ? "encoded" : "decoded")))
                .into(target);
        return target;
    }

    public static @Nullable
    Bitmap retrieveVideoFrameFromVideo(Context context, String videoPath) throws Throwable {
        Bitmap bitmap = null;
        MediaMetadataRetriever mediaMetadataRetriever = null;
        try {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(context, FileHelperKt.uri(new File(videoPath), context));
            bitmap = mediaMetadataRetriever.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mediaMetadataRetriever != null) {
                mediaMetadataRetriever.release();
            }
        }
        return bitmap;
    }

    public static void hideSoftKeyboard(Activity activity) {
        if (activity == null || activity.getWindow() == null || activity.getWindow().getDecorView() == null
                || activity.getWindow().getDecorView().getWindowToken() == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
    }

    public static Bitmap blurBitmap(Context context, Bitmap bitmap) {

        //Let's create an empty bitmap with the same size of the bitmap we want to blur
        Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        //Instantiate a new Renderscript
        RenderScript rs = RenderScript.create(context.getApplicationContext());

        //Create an Intrinsic Blur Script using the Renderscript
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

        //Create the Allocations (in/out) with the Renderscript and the in/out bitmaps
        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);

        //Set the radius of the blur
        blurScript.setRadius(25.f);

        //Perform the Renderscript
        blurScript.setInput(allIn);
        blurScript.forEach(allOut);

        //Copy the final bitmap created by the out Allocation to the outBitmap
        allOut.copyTo(outBitmap);

        //recycle the original bitmap
        bitmap.recycle();

        //After finishing everything, we destroy the Renderscript.
        rs.destroy();

        return outBitmap;
    }

    public static String getFileName(String filePath) {
        String[] components = filePath.split("/");
        if (components.length <= 0) return "";
        return components[components.length - 1];
    }
}

package com.ping.android.fragment;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ping.android.App;
import com.ping.android.activity.BeforeLoginActivity;
import com.ping.android.activity.BlockActivity;
import com.ping.android.activity.ChangePasswordActivity;
import com.ping.android.activity.MappingActivity;
import com.ping.android.activity.PrivacyAndTermActivity;
import com.ping.android.activity.R;
import com.ping.android.activity.TransphabetActivity;
import com.ping.android.model.User;
import com.ping.android.service.CallService;
import com.ping.android.service.NotificationService;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;
import com.ping.android.util.QBResRequestExecutor;
import com.ping.android.utils.Toaster;
import com.ping.android.utils.UiUtils;
import com.ping.android.utils.UsersUtils;

import java.io.File;
import java.io.FileOutputStream;

import me.leolin.shortcutbadger.ShortcutBadger;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment implements View.OnClickListener {

    private FirebaseAuth auth;
    private QBResRequestExecutor requestExecutor;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;

    private ImageView profileImage;
    private TextView tvName;
    private Switch rbNotification, rbShowProfile;

    private boolean loadData, loadGUI;
    private User currentUser;
    private String profileFileName, profileFileFolder, profileFilePath;
    private TextView tvDisplayName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ServiceManager.getInstance().initUserData(new Callback() {
            @Override
            public void complete(Object error, Object... data) {
                init();
                loadData = true;
                if (loadGUI) {
                    bindData();
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        bindViews(view);
        if (loadData & !loadGUI) {
            bindData();
        }
        loadGUI = true;
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        loadGUI = false;
    }

    private void bindViews(View view) {
        tvName = (TextView) view.findViewById(R.id.profile_name);
        tvDisplayName = (TextView) view.findViewById(R.id.tv_display_name);
        profileImage = (ImageView) view.findViewById(R.id.profile_image);
        profileImage.setImageResource(R.drawable.ic_avatar_gray);
        rbNotification = (Switch) view.findViewById(R.id.profile_notification);
        rbShowProfile = (Switch) view.findViewById(R.id.profile_show_profile);

        view.findViewById(R.id.profile_username_detail).setOnClickListener(this);
        view.findViewById(R.id.profile_phone_detail).setOnClickListener(this);
        view.findViewById(R.id.profile_mapping).setOnClickListener(this);
        view.findViewById(R.id.profile_change_password).setOnClickListener(this);
        view.findViewById(R.id.profile_block).setOnClickListener(this);
        view.findViewById(R.id.profile_help).setOnClickListener(this);
        view.findViewById(R.id.profile_privacy_and_terms).setOnClickListener(this);
        view.findViewById(R.id.profile_sign_out).setOnClickListener(this);
        view.findViewById(R.id.profile_image).setOnClickListener(this);
        view.findViewById(R.id.profile_notification).setOnClickListener(this);
        view.findViewById(R.id.profile_show_profile).setOnClickListener(this);
    }

    private void bindData() {
        if (currentUser != null) {
            tvName.setText(currentUser.pingID);
            tvDisplayName.setText(currentUser.getDisplayName());
        }

        UiUtils.displayProfileImage(getContext(), profileImage, currentUser, true);
        rbNotification.setChecked(currentUser.settings.notification);
        rbShowProfile.setChecked(currentUser.settings.private_profile);
    }

    private void init() {
        requestExecutor = App.getInstance().getQbResRequestExecutor();
        auth = FirebaseAuth.getInstance();
        mFirebaseUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
        storage = FirebaseStorage.getInstance();
        currentUser = ServiceManager.getInstance().getCurrentUser();
        profileFileFolder = getActivity().getExternalFilesDir(null).getAbsolutePath() + File.separator +
                "profile" + File.separator + currentUser.key;
        CommonMethod.createFolder(profileFileFolder);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.profile_username_detail:
                onViewUsername();
                break;
            case R.id.profile_phone_detail:
                onViewPassword();
                break;
            case R.id.profile_mapping:
                onEditMapping();
                break;
            case R.id.profile_change_password:
                onChangePwd();
                break;
            case R.id.profile_block:
                onOpenBlock();
                break;
            case R.id.profile_help:
                onOpenHelp();
                break;

            case R.id.profile_privacy_and_terms:
                onClickPrivacyAndTerms();
                break;
            case R.id.profile_sign_out:
                onLogout();
                break;
            case R.id.profile_image:
                onChangeProfile(view);
                break;
            case R.id.profile_notification:
                onNotificationClick();
                break;
            case R.id.profile_show_profile:
                onShowProfileClick();
                break;
        }
    }

    private void onViewUsername() {
        LayoutInflater li = LayoutInflater.from(getContext());
        View promptsView = li.inflate(R.layout.dialog_detail_username, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final TextView nameTv = (TextView) promptsView.findViewById(R.id.dialog_username_name);
        nameTv.setText("Username: " + currentUser.pingID);

        final TextView emailTv = (TextView) promptsView.findViewById(R.id.dialog_username_email);
        emailTv.setText("Email: " + currentUser.email);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK", null);

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void onViewPassword() {
        LayoutInflater li = LayoutInflater.from(getContext());
        View promptsView = li.inflate(R.layout.dialog_detail_phone, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final TextView phoneTv = (TextView) promptsView.findViewById(R.id.dialog_phone_number);
        phoneTv.setText("Primary: " + currentUser.phone);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK", null);

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void onEditMapping() {
        if (currentUser == null) {
            return;
        }
        startActivity(new Intent(getActivity(), TransphabetActivity.class));
    }

    private void onOpenBlock() {
        startActivity(new Intent(getActivity(), BlockActivity.class));
    }

    private void onNotificationClick() {
        currentUser.settings.notification = rbNotification.isChecked();
        ServiceManager.getInstance().updateSetting(currentUser.settings);
    }

    private void onShowProfileClick() {
        currentUser.settings.private_profile = rbShowProfile.isChecked();
        ServiceManager.getInstance().updateSetting(currentUser.settings);
    }

    private void onOpenHelp() {
        Intent termsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.URL_HELP));
        startActivity(termsIntent);
    }

    private void onClickPrivacyAndTerms() {
        Intent termsIntent = new Intent(getContext(), PrivacyAndTermActivity.class);
        startActivity(termsIntent);
    }

    private void onLogout() {
        if (auth == null) {
            auth = FirebaseAuth.getInstance();
        }
        auth.signOut();

        //TODO enable notification
        Intent intent = new Intent(getContext(), NotificationService.class);
        intent.putExtra("OBSERVE_FLAG", false);
        getContext().startService(intent);

        CallService.logout(getContext());
        UsersUtils.removeUserData(getActivity().getApplicationContext());
        //ServiceManager.getInstance().logoutQB();

        ServiceManager.getInstance().updateLoginStatus(false);
        ShortcutBadger.applyCount(getActivity(), 0);
        getActivity().finish();
        startActivity(new Intent(getActivity(), BeforeLoginActivity.class));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.SELECT_IMAGE_REQUEST) {
            if (resultCode == RESULT_OK) {
                Uri selectedImageUri = data.getData();
                performCrop(selectedImageUri);
            }
        }

        if (requestCode == Constant.CROP_IMAGE_REQUEST) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                if (data.hasExtra("data")) {
                    Bitmap selectedBitmap = extras.getParcelable("data");
                    saveImage(selectedBitmap);
                    UiUtils.displayProfileAvatar(profileImage, profileFilePath);
                }
                uploadProfile();
            }
        }
    }

    private void onEditMapping(View view) {
        if (!ServiceManager.getInstance().getNetworkStatus(getContext())) {
            Toaster.shortToast("Please check network connection.");
            return;
        }
        if (currentUser == null) {
            return;
        }
        startActivity(new Intent(getActivity(), MappingActivity.class));
    }

    private void onChangePwd() {
        if (!ServiceManager.getInstance().getNetworkStatus(getContext())) {
            Toaster.shortToast("Please check network connection.");
            return;
        }
        startActivity(new Intent(getActivity(), ChangePasswordActivity.class));
    }

    private void onChangeProfile(View view) {
        if (!ServiceManager.getInstance().getNetworkStatus(getContext())) {
            Toaster.shortToast("Please check network connection.");
            return;
        }

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Get photo"), Constant.SELECT_IMAGE_REQUEST);

    }

    private void performCrop(Uri contentUri) {
        try {
            File file = getMediaFileFromUri(getContext(), contentUri);
            if (file == null) {
                return;
            }
            //Start Crop Activity
            Intent cropIntent = new Intent("com.android.camera.action.CROP");

            cropIntent.setDataAndType(Uri.fromFile(file), "image/*");
            // set crop properties
            cropIntent.putExtra("crop", "true");
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            // indicate output X and Y
            cropIntent.putExtra("outputX", 280);
            cropIntent.putExtra("outputY", 280);

            // retrieve data on return
            cropIntent.putExtra("return-data", true);
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, Constant.CROP_IMAGE_REQUEST);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
        }
    }

    private void saveImage(Uri uri, String localPath) {
        FileOutputStream out = null;
        try {
            File file = getMediaFileFromUri(getContext(), uri);
            if (file != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                out = new FileOutputStream(localPath);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e1) {
            }
        }
    }

    private File getMediaFileFromUri(Context context, Uri uri) {
        String selection = null;
        String[] selectionArgs = null;
        String[] projection;
        String column = null;
        Uri contentUri = uri;
        if (DocumentsContract.isDocumentUri(context, uri)) {
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];
            if ("image".equals(type)) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                column = MediaStore.Images.Media.DATA;
            } else if ("video".equals(type)) {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                column = MediaStore.Video.Media.DATA;
            }
            selection = MediaStore.Images.Media._ID + "=?";
            selectionArgs = new String[]{
                    split[1]
            };

        } else {
            column = MediaStore.Images.Media.DATA;
        }
        projection = new String[]{column};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(contentUri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int columnIndex = cursor.getColumnIndexOrThrow(column);
                String path = cursor.getString(columnIndex);
                if (!TextUtils.isEmpty(path)) {
                    return new File(path);
                }
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private void saveImage(Bitmap bitmap) {
        Long timestamp = System.currentTimeMillis() / 1000L;
        profileFileName = "" + timestamp + "-" + currentUser.key + ".png";
        profileFilePath = profileFileFolder + File.separator + profileFileName;
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(profileFilePath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e1) {
            }
        }
    }

    private void uploadProfile() {
        String imageStoragePath = "profile" + File.separator + currentUser.key + File.separator + profileFileName;
        StorageReference photoRef = storage.getReferenceFromUrl(Constant.URL_STORAGE_REFERENCE).child(imageStoragePath);
        UploadTask uploadTask = photoRef.putFile(Uri.fromFile(new File(profileFilePath)));
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                String downloadUrl = Constant.URL_STORAGE_REFERENCE + "/" + taskSnapshot.getMetadata().getPath();
                ServiceManager.getInstance().updateProfile(downloadUrl);
            }
        });
    }
}
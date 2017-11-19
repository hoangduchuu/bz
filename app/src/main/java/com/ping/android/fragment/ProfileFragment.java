package com.ping.android.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
                    saveImage(selectedBitmap, profileFilePath);
                }
                // Set The Bitmap Data To ImageView
                Bitmap originalBitmap = BitmapFactory.decodeFile(profileFilePath);
                profileImage.setImageBitmap(originalBitmap);
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
            Long timestamp = System.currentTimeMillis() / 1000L;
            profileFileName = "" + timestamp + "-" + currentUser.key + ".png";
            profileFilePath = profileFileFolder + File.separator + profileFileName;

            saveImage(contentUri, profileFilePath);

            //Start Crop Activity
            Intent cropIntent = new Intent("com.android.camera.action.CROP");

            cropIntent.setDataAndType(Uri.fromFile(new File(profileFilePath)), "image/*");
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
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
            out = new FileOutputStream(localPath);
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

    private void saveImage(Bitmap bitmap, String localPath) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(localPath);
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
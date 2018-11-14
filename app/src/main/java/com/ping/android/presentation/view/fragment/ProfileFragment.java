package com.ping.android.presentation.view.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;

import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bzzzchat.videorecorder.view.facerecognition.FaceRecognition;
import com.bzzzchat.videorecorder.view.facerecognition.FaceTrainingActivity;
import com.ping.android.R;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.ProfilePresenter;
import com.ping.android.presentation.view.activity.BlockActivity;
import com.ping.android.presentation.view.activity.ChangePasswordActivity;
import com.ping.android.presentation.view.activity.PrivacyAndTermActivity;
import com.ping.android.presentation.view.activity.RegistrationActivity;
import com.ping.android.presentation.view.activity.TransphabetActivity;
import com.ping.android.presentation.view.custom.SettingItem;
import com.ping.android.service.CallService;
import com.ping.android.utils.CommonMethod;
import com.ping.android.utils.ImagePickerHelper;
import com.ping.android.utils.Log;
import com.ping.android.utils.SharedPrefsHelper;
import com.ping.android.utils.Toaster;
import com.ping.android.utils.UiUtils;
import com.ping.android.utils.UsersUtils;
import com.ping.android.utils.configs.Constant;
import com.quickblox.messages.services.SubscribeService;

import java.io.File;
import java.util.Objects;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import me.leolin.shortcutbadger.ShortcutBadger;

public class ProfileFragment extends BaseFragment implements View.OnClickListener, ProfilePresenter.View {
    private ImagePickerHelper imagePickerHelper;

    private ImageView profileImage;
    private TextView tvName;
    private SwitchCompat rbNotification, rbShowProfile;
    private SwitchCompat faceId;
    private SettingItem faceTrainingItem;

    private User currentUser;
    private String profileFileName, profileFileFolder, profileFilePath;
    private TextView tvDisplayName;

    private Boolean isTrained = false;
    @Inject
    ProfilePresenter presenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidSupportInjection.inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        bindViews(view);
        presenter.create();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.destroy();
    }

    private void bindViews(View view) {
        tvName = view.findViewById(R.id.profile_name);
        tvDisplayName = view.findViewById(R.id.tv_display_name);
        profileImage = view.findViewById(R.id.profile_image);
        rbNotification = view.findViewById(R.id.profile_notification);
        rbShowProfile = view.findViewById(R.id.profile_show_profile);
        faceId = view.findViewById(R.id.profile_faceid);
        faceTrainingItem = view.findViewById(R.id.profile_face_training);

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
        view.findViewById(R.id.profile_face_training).setOnClickListener(this);
        faceId.setOnClickListener(this);

        faceId.setChecked(SharedPrefsHelper.getInstance().isFaceIdEnable());
        if (!SharedPrefsHelper.getInstance().isFaceIdEnable()) {
            faceTrainingItem.setVisibility(View.GONE);
        } else {
            showFaceTrainingItem();
        }

    }

    private void bindData() {
        tvName.setText(currentUser.pingID);
        tvDisplayName.setText(currentUser.getDisplayName());

        UiUtils.displayProfileImage(getContext(), profileImage, currentUser, R.drawable.ic_avatar_light, true);
        rbNotification.setChecked(currentUser.settings.notification);
        rbShowProfile.setChecked(currentUser.settings.private_profile);
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
            case R.id.profile_face_training:
                onTrainingFaceClicked();
                break;
            case R.id.profile_faceid:
                onSelectFaceId();
                break;
        }
    }

    private void onSelectFaceId() {
        SharedPrefsHelper.getInstance().setFaceIdEnable(faceId.isChecked());
        TransitionManager.beginDelayedTransition((ViewGroup) getView());
        if (!faceId.isChecked()) {
            faceTrainingItem.setVisibility(View.GONE);
            SharedPrefsHelper.getInstance().setFaceIdCompleteTraining(false);
            FaceRecognition.Companion.getInstance(this.getContext()).removeTrainingData();
        } else {
            showFaceTrainingItem();
        }
    }

    private void showFaceTrainingItem() {
        faceTrainingItem.setVisibility(View.VISIBLE);
        if (SharedPrefsHelper.getInstance().isFaceIdCompleteTraining()) {
            faceTrainingItem.setTitle(getString(R.string.profile_delete_face_trained));
        } else {
            faceTrainingItem.setTitle(getString(R.string.profile_face_training));
        }
    }

    private void onTrainingFaceClicked() {
//        if (SharedPrefsHelper.getInstance().isFaceIdCompleteTraining()) {
//            SharedPrefsHelper.getInstance().setFaceIdCompleteTraining(false);
//            showFaceTrainingItem();
//        }
        if (SharedPrefsHelper.getInstance().isFaceIdCompleteTraining()){
            showRequirePasswordForm();
        }

        else {
            Intent intent = new Intent(getContext(), FaceTrainingActivity.class);
            startActivityForResult(intent, 1111);
//            Intent intent = new Intent(getContext(), AddPersonPreviewActivity.class);
//            intent.putExtra("Name", "admin");
//            intent.putExtra("Method", AddPersonPreviewActivity.TIME);
//            intent.putExtra("Folder", "Training");
//            startActivity(intent);
        }
    }

    @Override
    public ProfilePresenter getPresenter() {
        return presenter;
    }

    private void onViewUsername() {
        LayoutInflater li = LayoutInflater.from(getContext());
        @SuppressLint("InflateParams") View promptsView = li.inflate(R.layout.dialog_detail_username, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final TextView nameTv = promptsView.findViewById(R.id.dialog_username_name);
        nameTv.setText(String.format("%s %s", getString(R.string.profile_user_name), currentUser.pingID));

        final TextView emailTv = promptsView.findViewById(R.id.dialog_username_email);
        emailTv.setText(String.format("%s %s", getString(R.string.profile_email), currentUser.email));

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(getString(R.string.profile_ok), null);

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

        final TextView phoneTv = promptsView.findViewById(R.id.dialog_phone_number);
        phoneTv.setText(String.format("%s%s", getString(R.string.profile_primary), currentUser.phone));

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(getString(R.string.profile_ok), null);

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
        presenter.toggleNotificationSetting(rbNotification.isChecked());
    }

    private void onShowProfileClick() {
        presenter.togglePrivateProfileSetting(rbShowProfile.isChecked());
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
        presenter.logout();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (imagePickerHelper != null) {
            imagePickerHelper.onActivityResult(requestCode, resultCode, data);
        }
        if (requestCode == 1111) {
            if (resultCode == Activity.RESULT_OK) {
                SharedPrefsHelper.getInstance().setFaceIdCompleteTraining(true);
                showFaceTrainingItem();
                FaceRecognition.Companion.getInstance(this.getContext()).train();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (imagePickerHelper != null) {
            imagePickerHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void onChangePwd() {
        if (!isNetworkAvailable()) {
            Toaster.shortToast(getString(R.string.profile_check_network));
            return;
        }
        startActivity(new Intent(getActivity(), ChangePasswordActivity.class));
    }

    private void onChangeProfile(View view) {
        if (!isNetworkAvailable()) {
            Toaster.shortToast(getString(R.string.profile_check_network));
            return;
        }
        profileFileFolder = Objects.requireNonNull(getActivity().getExternalFilesDir(null)).getAbsolutePath() + File.separator +
                "profile" + File.separator + currentUser.key;
        CommonMethod.createFolder(profileFileFolder);
        double timestamp = System.currentTimeMillis() / 1000d;
        profileFileName = "" + timestamp + "-" + currentUser.key + ".jpeg";
        profileFilePath = profileFileFolder + File.separator + profileFileName;

        imagePickerHelper = ImagePickerHelper.from(this)
                .setFilePath(profileFilePath)
                .setCrop(true)
                .setListener(new ImagePickerHelper.ImagePickerListener() {
                    @Override
                    public void onImageReceived(File file) {

                    }

                    @Override
                    public void onFinalImage(File... files) {
                        File imagePath = files[0];
                        UiUtils.displayProfileAvatar(profileImage, imagePath);
                        uploadProfile();
                    }
                });
        imagePickerHelper.openPicker();
    }

    private void uploadProfile() {
        presenter.uploadUserProfile(profileFilePath);
    }

    @Override
    public void updateUser(User user) {
        this.currentUser = user;
        bindData();
    }

    @Override
    public void navigateToLogin() {
        SubscribeService.unSubscribeFromPushes(getContext());
        CallService.logout(getContext());

        UsersUtils.removeUserData(getContext());
        ShortcutBadger.applyCount(getActivity(), 0);
        startActivity(new Intent(getActivity(), RegistrationActivity.class));
        Objects.requireNonNull(getActivity()).finish();
    }

    @Override
    public void showErrorLogoutFailed() {
        AlertDialog dialog = new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                .setTitle(getString(R.string.profile_logout_falied))
                .setMessage(getString(R.string.profile_try_again_later))
                .setPositiveButton(getString(R.string.profile_ok), (dialog1, which) -> dialog1.dismiss())
                .create();
        dialog.show();
    }

    @Override
    public void showRequirePasswordForm() {
        View promptsView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_check_password, null);
        EditText password = promptsView.findViewById(R.id.tvPassword);
        AlertDialog dialog = new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                .setTitle("")
                .setView(promptsView)
                .setPositiveButton(getString(R.string.profile_send), (dialog12, which) -> {
                         presenter.checkPassword(password.getText().toString().trim());
                })
                .setNegativeButton(getString(R.string.profile_cancel), (dialog1, which) -> dialog1.dismiss())
                .create();
        Objects.requireNonNull(dialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    @Override
    public void handleConfirmPasswordError(String errorMsg) {
        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void handleConfirmPasswordSuccess() {
        Toast.makeText(getContext(), "handleConfirmPasswordSuccess", Toast.LENGTH_SHORT).show();

    }
}
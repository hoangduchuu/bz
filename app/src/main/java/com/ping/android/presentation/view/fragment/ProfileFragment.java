package com.ping.android.presentation.view.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.ping.android.presentation.view.activity.BeforeLoginActivity;
import com.ping.android.presentation.view.activity.BlockActivity;
import com.ping.android.presentation.view.activity.ChangePasswordActivity;
import com.ping.android.presentation.view.activity.PrivacyAndTermActivity;
import com.ping.android.R;
import com.ping.android.presentation.view.activity.TransphabetActivity;
import com.ping.android.dagger.loggedin.main.MainComponent;
import com.ping.android.dagger.loggedin.main.profile.ProfileComponent;
import com.ping.android.dagger.loggedin.main.profile.ProfileModule;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.ProfilePresenter;
import com.ping.android.service.CallService;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.ImagePickerHelper;
import com.ping.android.utils.Toaster;
import com.ping.android.utils.UiUtils;
import com.ping.android.utils.UsersUtils;
import com.quickblox.messages.services.SubscribeService;

import java.io.File;

import javax.inject.Inject;

import me.leolin.shortcutbadger.ShortcutBadger;

public class ProfileFragment extends BaseFragment implements View.OnClickListener, ProfilePresenter.View {
    private ImagePickerHelper imagePickerHelper;

    private ImageView profileImage;
    private TextView tvName;
    private Switch rbNotification, rbShowProfile;

    private User currentUser;
    private String profileFileName, profileFileFolder, profileFilePath;
    private TextView tvDisplayName;

    @Inject
    ProfilePresenter presenter;
    ProfileComponent component;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getComponent().inject(this);
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
        profileImage.setImageResource(R.drawable.ic_avatar_gray);
        rbNotification = view.findViewById(R.id.profile_notification);
        rbShowProfile = view.findViewById(R.id.profile_show_profile);

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
        tvName.setText(currentUser.pingID);
        tvDisplayName.setText(currentUser.getDisplayName());

        UiUtils.displayProfileImage(getContext(), profileImage, currentUser, true);
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
        }
    }

    @Override
    public ProfilePresenter getPresenter() {
        return presenter;
    }

    private void onViewUsername() {
        LayoutInflater li = LayoutInflater.from(getContext());
        View promptsView = li.inflate(R.layout.dialog_detail_username, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final TextView nameTv = promptsView.findViewById(R.id.dialog_username_name);
        nameTv.setText("Username: " + currentUser.pingID);

        final TextView emailTv = promptsView.findViewById(R.id.dialog_username_email);
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

        final TextView phoneTv = promptsView.findViewById(R.id.dialog_phone_number);
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
        SubscribeService.unSubscribeFromPushes(getContext());
        CallService.logout(getContext());

        UsersUtils.removeUserData(getContext());
        ShortcutBadger.applyCount(getActivity(), 0);
        getActivity().finish();
        startActivity(new Intent(getActivity(), BeforeLoginActivity.class));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (imagePickerHelper != null) {
            imagePickerHelper.onActivityResult(requestCode, resultCode, data);
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
        profileFileFolder = getActivity().getExternalFilesDir(null).getAbsolutePath() + File.separator +
                "profile" + File.separator + currentUser.key;
        CommonMethod.createFolder(profileFileFolder);
        double timestamp = System.currentTimeMillis() / 1000d;
        profileFileName = "" + timestamp + "-" + currentUser.key + ".png";
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

    public ProfileComponent getComponent() {
        if (component == null) {
            component = getComponent(MainComponent.class).provideProfileComponent(new ProfileModule(this));
        }
        return component;
    }

    @Override
    public void updateUser(User user) {
        this.currentUser = user;
        bindData();
    }
}
package com.ping.android.presentation.view.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bzzzchat.cleanarchitecture.UIThread;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.ping.android.R;
import com.ping.android.model.User;
import com.ping.android.model.enums.NetworkStatus;
import com.ping.android.presentation.presenters.AddGroupPresenter;
import com.ping.android.presentation.presenters.SearchUserPresenter;
import com.ping.android.presentation.view.adapter.SelectContactAdapter;
import com.ping.android.presentation.view.custom.ChipsEditText;
import com.ping.android.utils.ImagePickerHelper;
import com.ping.android.utils.Toaster;
import com.ping.android.utils.UiUtils;
import com.ping.android.utils.configs.Constant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class AddGroupActivity extends CoreActivity implements View.OnClickListener, SearchUserPresenter.View, AddGroupPresenter.View {
    private LinearLayoutManager mLinearLayoutManager;
    private EditText etGroupName, edMessage;
    private Button btSave, btSendMessage;
    private ImageView btBack;
    private ImageView groupAvatar;
    private ChipsEditText edtTo;
    private RecyclerView recycleChatView;
    private LinearLayout noResultsView;
    private View loading;

    private ImagePickerHelper imagePickerHelper;
    private File groupProfileImage = null;

    private SelectContactAdapter adapter;
    private ArrayList<User> selectedUsers = new ArrayList<>();

    @Inject
    public SearchUserPresenter searchPresenter;
    @Inject
    public AddGroupPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);
        AndroidInjection.inject(this);
        searchPresenter.create();
        presenter.create();
        bindViews();
        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        searchPresenter.destroy();
    }

    private void bindViews() {
        edtTo = findViewById(R.id.edtTo);
        etGroupName = findViewById(R.id.new_group_name);
        btBack = findViewById(R.id.new_group_back);
        btBack.setOnClickListener(this);
        btSave = findViewById(R.id.new_group_save);
        btSave.setOnClickListener(this);
        groupAvatar = findViewById(R.id.profile_image);
        groupAvatar.setOnClickListener(this);

        noResultsView = findViewById(R.id.no_results);

        edMessage = findViewById(R.id.new_group_message_tv);
        btSendMessage = findViewById(R.id.new_group_send_message_btn);
        btSendMessage.setOnClickListener(this);
        loading = findViewById(R.id.spin_kit);

        findViewById(R.id.new_group_select_contact).setOnClickListener(this);

        recycleChatView = findViewById(R.id.chat_list_view);
        mLinearLayoutManager = new LinearLayoutManager(this);
        recycleChatView.setLayoutManager(mLinearLayoutManager);

        edMessage.setOnFocusChangeListener((view, b) -> {
            if (b) {
                adapter.updateData(new ArrayList<>());
            }
        });
        registerEvent(edtTo.chipEventObservable()
                .observeOn(new UIThread().getScheduler())
                .subscribe(chipEvent -> {
                    switch (chipEvent.type) {
                        case SEARCH:
                            if (TextUtils.isEmpty(chipEvent.text)) {
                                adapter.updateData(new ArrayList<>());
                            } else {
                                searchPresenter.searchUsers(chipEvent.text);
                            }
                            break;
                        case DELETE:
                            for (User user : selectedUsers) {
                                if (user.getDisplayName().equals(chipEvent.text)) {
                                    selectedUsers.remove(user);
                                    adapter.setSelectPingIDs(getSelectedPingId());
                                    break;
                                }
                            }
                            break;
                    }
                }));

        registerEvent(RxTextView
                .afterTextChangeEvents(edMessage)
                .subscribe(textViewAfterTextChangeEvent -> checkReadySend()));
        registerEvent(RxTextView
                .afterTextChangeEvents(etGroupName)
                .subscribe(textViewAfterTextChangeEvent -> checkReadySend()));
        checkReadySend();
    }

    private List<String> getSelectedPingId() {
        List<String> selectedPingId = new ArrayList<>();
        for (User user : selectedUsers) {
            selectedPingId.add(user.pingID);
        }
        return selectedPingId;
    }

    private void init() {
        adapter = new SelectContactAdapter(new ArrayList<>(), (contact, isSelected) -> {
            if (isSelected) {
                selectedUsers.add(contact);
                updateChips();
            } else {
                for (User user : selectedUsers) {
                    if (user.key.equals(contact.key)) {
                        selectedUsers.remove(user);
                        updateChips();
                        break;
                    }
                }
            }
        });
        recycleChatView.setAdapter(adapter);
    }

    private void updateChips() {
        StringBuilder builder = new StringBuilder();
        for (User user : selectedUsers) {
            builder.append(user.getDisplayName());
            builder.append(",");
        }
        edtTo.updateText(builder.toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.new_group_save:
                onCreateGroup();
                break;
            case R.id.new_group_back:
                onCancelGroup();
                break;
            case R.id.new_group_select_contact:
                selectContact();
                break;
            case R.id.new_group_send_message_btn:
                onCreateGroup();
                break;
            case R.id.profile_image:
                presenter.handlePickerPress();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (imagePickerHelper != null) {
            imagePickerHelper.onActivityResult(requestCode, resultCode, data);
        }
        if (requestCode == Constant.SELECT_CONTACT_REQUEST) {
            if (resultCode == RESULT_OK) {
                selectedUsers = data.getParcelableArrayListExtra(SelectContactActivity.SELECTED_USERS_KEY);
                updateChips();
                adapter.setSelectPingIDs(getSelectedPingId());
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (imagePickerHelper != null) {
            imagePickerHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void checkReadySend() {
        if (TextUtils.isEmpty(edMessage.getText().toString().trim())
                || selectedUsers.size() <= 0
                || TextUtils.isEmpty(etGroupName.getText().toString().trim())) {
            btSendMessage.setEnabled(false);
        } else {
            btSendMessage.setEnabled(true);
        }
    }

    private void selectContact() {
        Intent i = new Intent(this, SelectContactActivity.class);
        i.putParcelableArrayListExtra("SELECTED_USERS", selectedUsers);
        startActivityForResult(i, Constant.SELECT_CONTACT_REQUEST);
    }

    private void onCreateGroup() {
        String groupNames = etGroupName.getText().toString().trim();
        if (TextUtils.isEmpty(groupNames)) {
            Toaster.shortToast("Name this group.");
            return;
        }
        if (networkStatus != NetworkStatus.CONNECTED) {
            Toaster.shortToast("Please check network connection.");
            return;
        }
        presenter.createGroup(selectedUsers, groupNames,
                groupProfileImage != null ? groupProfileImage.getAbsolutePath() : "",
                edMessage.getText().toString());
    }

    private void onCancelGroup() {
        finish();
    }

    @Override
    public void openPicker() {
        imagePickerHelper.openPicker();
    }

    @Override
    public void showSearching() {
        loading.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideSearching() {
        loading.setVisibility(View.GONE);
    }

    @Override
    public void showNoResults() {
        adapter.updateData(new ArrayList<>());
        noResultsView.post(() -> noResultsView.setVisibility(View.VISIBLE));
    }

    @Override
    public void hideNoResults() {
        noResultsView.post(() -> noResultsView.setVisibility(View.GONE));
    }

    @Override
    public void displaySearchResult(List<User> users) {
        adapter.updateData(new ArrayList<>(users));
    }

    @Override
    public void moveToChatScreen(String conversationID) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.CONVERSATION_ID, conversationID);
        startActivity(intent);
        finish();
    }

    @Override
    public void initProfileImagePath(String key) {
        String parentPath = getExternalFilesDir(null).getAbsolutePath() + File.separator + "profile" +  File.separator + key;
        File parent = new File(parentPath);
        if (!parent.exists()) {
            parent.mkdirs();
        }
        double timestamp = System.currentTimeMillis() / 1000d;
        String profileFileName = "" + timestamp + "-" + key + ".jpeg";
        String profileFilePath = parentPath + File.separator + profileFileName;
        imagePickerHelper = ImagePickerHelper.from(this)
                .setFilePath(profileFilePath)
                .setCrop(true)
                .setListener(new ImagePickerHelper.ImagePickerListener() {
                    @Override
                    public void onImageReceived(File file) {

                    }

                    @Override
                    public void onFinalImage(File... files) {
                        groupProfileImage = files[0];
                        UiUtils.displayProfileAvatar(groupAvatar, groupProfileImage);
                    }
                });
    }
}

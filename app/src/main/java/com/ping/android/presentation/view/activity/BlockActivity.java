package com.ping.android.presentation.view.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ping.android.R;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.BlockContactPresenter;
import com.ping.android.presentation.view.adapter.BlockAdapter;
import com.ping.android.utils.configs.Constant;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class BlockActivity extends CoreActivity implements View.OnClickListener, BlockAdapter.ClickListener, BlockContactPresenter.View {
    private final String TAG = "Ping: " + this.getClass().getSimpleName();

    private RecyclerView rvListBlock;
    private LinearLayoutManager mLinearLayoutManager;
    private Button actionButton;
    private LinearLayout bottomLayout;
    private TextView bottomText;

    private BlockAdapter adapter;
    private List<String> blockIds = new ArrayList<>();

    @Inject
    BlockContactPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block);
        AndroidInjection.inject(this);
        bindViews();
        init();
        presenter.create();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public BlockContactPresenter getPresenter() {
        return presenter;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constant.SELECT_CONTACT_REQUEST) {
            if (resultCode == RESULT_OK) {
                //ArrayList<String> selectContacts = data.getStringArrayListExtra("SELECT_CONTACT_USER_IDS");
                //blockContact(selectContacts);
                //blockIds.addAll(selectContacts);
                List<User> selectedUsers = data.getParcelableArrayListExtra(SelectContactActivity.SELECTED_USERS_KEY);
                blockContact(selectedUsers);
            }
        }
    }

    private void bindViews() {
        rvListBlock = findViewById(R.id.block_list_view);
        mLinearLayoutManager = new LinearLayoutManager(this);

        findViewById(R.id.iv_back).setOnClickListener(this);
        actionButton = findViewById(R.id.block_save);
        actionButton.setOnClickListener(this);
        bottomLayout = findViewById(R.id.block_add);
        bottomLayout.setOnClickListener(this);
        bottomText = findViewById(R.id.bottom_text);
    }

    private void init() {
        adapter = new BlockAdapter(new ArrayList<>(), this, this);
        rvListBlock.setAdapter(adapter);
        rvListBlock.setLayoutManager(mLinearLayoutManager);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                exit();
                break;
            case R.id.block_save:
                onEditPress();
                break;
            case R.id.block_add:
                if (adapter.isInEditMode()) {
                    unBlockContact(adapter.getSelectedContact());
                    rvListBlock.postDelayed(this::onEditPress, 500);
                } else {
                    add();
                }
                break;
        }
    }


    private void onEditPress() {
        adapter.toggleEditMode();
        this.refreshView(adapter.isInEditMode());
    }

    private void refreshView(boolean isEditMode) {
        actionButton.setText(isEditMode ? "CANCEL" : "EDIT");
        bottomText.setText(isEditMode ? "UNBLOCK" : "ADD NEW");

    }

    private void add() {
        Intent i = new Intent(this, SelectContactActivity.class);
        i.putExtra("SELECTED_ID", TextUtils.join(",", blockIds));
        startActivityForResult(i, Constant.SELECT_CONTACT_REQUEST);
    }

    private void blockContact(List<User> contacts) {
        for (User contact : contacts) {
            presenter.toggleBlockUser(contact.key, true);
        }
    }

    private void unBlockContact(List<User> contacts) {
        for (User contact : contacts) {
            presenter.toggleBlockUser(contact.key, false);
        }
    }

    @Override
    public void removeBlockedUser(String key) {
        adapter.removeContact(key);
        if (adapter.getItemCount() == 0) {
            actionButton.setEnabled(false);
        }
    }

    @Override
    public void addBlockedUser(User data) {
        adapter.addContact(data);
        actionButton.setEnabled(true);
        blockIds.add(data.key);
    }
}

package com.ping.android.activity;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.ping.android.adapter.NicknameAdapter;
import com.ping.android.model.Conversation;
import com.ping.android.model.Nickname;
import com.ping.android.model.User;
import com.ping.android.service.firebase.ConversationRepository;
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.Constant;

import java.util.ArrayList;
import java.util.List;

public class NicknameActivity extends CoreActivity implements NicknameAdapter.NickNameListener {
    public static final String CONVERSATION_KEY = "CONVERSATION_KEY";

    private RecyclerView recyclerView;

    private Conversation conversation;
    private NicknameAdapter adapter;

    private ConversationRepository conversationRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nickname);
        conversation = getIntent().getParcelableExtra(CONVERSATION_KEY);
        conversationRepository = new ConversationRepository();
        initView();
    }

    private void initView() {
        recyclerView = findViewById(R.id.nickname_list);
        adapter = new NicknameAdapter(getNicknameList(conversation));
        adapter.setListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.iv_back).setOnClickListener(view -> onBackPressed());
    }

    private List<Nickname> getNicknameList(Conversation conversation) {
        List<Nickname> nicknames = new ArrayList<>();
        for (User user: conversation.members) {
            Nickname nickname = new Nickname();
            nickname.userId = user.key;
            nickname.displayName = user.getDisplayName();
            nickname.nickName = conversation.nickNames.containsKey(user.key) ? conversation.nickNames.get(user.key) : "";
            nickname.imageUrl = user.profile;
            nicknames.add(nickname);
        }
        return nicknames;
    }

    @Override
    public void onClick(Nickname nickname) {
        View promptsView = getLayoutInflater().inflate(R.layout.dialog_edit_nickname, null);
        TextView tvDescription = promptsView.findViewById(R.id.tv_description);
        if (conversation.conversationType == Constant.CONVERSATION_TYPE_INDIVIDUAL) {
            tvDescription.setText(String.format(getString(R.string.nick_name_edit_individual_description), conversation.opponentUser.getDisplayName()));
        } else {
            tvDescription.setText(R.string.nick_name_edit_group_description);
        }
        EditText edtNickname = promptsView.findViewById(R.id.edt_nickname);
        edtNickname.setText(nickname.nickName);
        edtNickname.setSelection(0, nickname.nickName.length());
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptsView);
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.gen_save,
                        (dialog, id) -> {
                            onNicknameSet(nickname, edtNickname.getText().toString());
                        })
                .setNegativeButton(R.string.gen_cancel,
                        (dialog, id) -> dialog.cancel());
        alertDialogBuilder.create().show();
    }

    private void onNicknameSet(Nickname nickname, String s) {
        nickname.nickName = s;
        showLoading();
        conversationRepository.updateUserNickname(conversation.key, nickname, conversation.memberIDs, (error, data) -> {
            if (error == null) {
                recyclerView.post(() -> adapter.updateNickName(nickname));
            }
            hideLoading();
        });
    }
}

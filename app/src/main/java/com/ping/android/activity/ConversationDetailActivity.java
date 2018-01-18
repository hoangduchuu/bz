package com.ping.android.activity;

import android.app.Activity;
import android.os.Bundle;

import com.ping.android.fragment.ConversationPVPDetailFragment;
import com.ping.android.utils.Navigator;

public class ConversationDetailActivity extends CoreActivity {
    public static final String USER_KEY = "USER_KEY";
    public static final String CONVERSATION_KEY = "CONVERSATION_KEY";

    Navigator navigator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_detail);
        navigator = new Navigator();
        navigator.init(getSupportFragmentManager(), R.id.fragment_container);

        Bundle extras = getIntent().getExtras();
        if (savedInstanceState == null) {
            navigator.openAsRoot(ConversationPVPDetailFragment.newInstance(extras));
        }
    }

    @Override
    public void onBackPressed() {
        navigator.navigateBack(this);
    }

    public Navigator getNavigator() {
        return navigator;
    }
}

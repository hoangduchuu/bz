package com.ping.android.presentation.view.activity;

import android.os.Bundle;

import com.bzzzchat.cleanarchitecture.scopes.HasComponent;
import com.ping.android.activity.CoreActivity;
import com.ping.android.activity.R;
import com.ping.android.dagger.loggedin.conversationdetail.ConversationDetailComponent;
import com.ping.android.presentation.view.fragment.ConversationGroupDetailFragment;
import com.ping.android.presentation.view.fragment.ConversationPVPDetailFragment;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.Navigator;

public class ConversationDetailActivity extends CoreActivity implements HasComponent<ConversationDetailComponent> {
    public static final String CONVERSATION_KEY = "CONVERSATION_KEY";
    public static final String CONVERSATION_TYPE_KEY = "CONVERSATION_TYPE_KEY";

    Navigator navigator;

    ConversationDetailComponent component;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_detail);
        postponeEnterTransition();
        navigator = new Navigator();
        navigator.init(getSupportFragmentManager(), R.id.fragment_container);

        Bundle extras = getIntent().getExtras();
        int conversationType = extras.getInt(CONVERSATION_TYPE_KEY);
        if (savedInstanceState == null) {
            if (conversationType == Constant.CONVERSATION_TYPE_INDIVIDUAL) {
                navigator.openAsRoot(ConversationPVPDetailFragment.newInstance(extras));
            } else {
                navigator.openAsRoot(ConversationGroupDetailFragment.newInstance(extras));
            }
        }
    }

    @Override
    public void onBackPressed() {
        navigator.navigateBack(this);
    }

    public Navigator getNavigator() {
        return navigator;
    }

    @Override
    public ConversationDetailComponent getComponent() {
        if (component == null) {
            component = getLoggedInComponent().provideConversationDetailComponent();
        }
        return component;
    }
}

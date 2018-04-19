package com.ping.android.presentation.view.activity;

import android.os.Bundle;

import com.bzzzchat.cleanarchitecture.scopes.HasComponent;
import com.ping.android.R;
import com.ping.android.dagger.loggedin.conversationdetail.ConversationDetailComponent;
import com.ping.android.model.enums.Color;
import com.ping.android.presentation.view.fragment.BaseFragment;
import com.ping.android.presentation.view.fragment.ConversationGroupDetailFragment;
import com.ping.android.presentation.view.fragment.ConversationPVPDetailFragment;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.Navigator;
import com.ping.android.utils.ThemeUtils;

import javax.inject.Inject;

public class ConversationDetailActivity extends CoreActivity implements HasComponent<ConversationDetailComponent> {
    public static final String CONVERSATION_KEY = "CONVERSATION_KEY";
    public static final String CONVERSATION_TYPE_KEY = "CONVERSATION_TYPE_KEY";
    public static final String EXTRA_IMAGE_KEY = "EXTRA_IMAGE_KEY";
    /**
     * Params used for gallery grid and viewpager
     */
    public static int currentPosition = 0;

    @Inject
    Navigator navigator;

    ConversationDetailComponent component;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getComponent().inject(this);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey(ChatActivity.EXTRA_CONVERSATION_COLOR)) {
                int color = bundle.getInt(ChatActivity.EXTRA_CONVERSATION_COLOR);
                Color currentColor = Color.from(color);
                ThemeUtils.onActivityCreateSetTheme(this, currentColor);
            }
        }
        setContentView(R.layout.activity_conversation_detail);
        postponeEnterTransition();

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
        BaseFragment fragment = navigator.getCurrentFragment();
        if (fragment != null) {
            if (fragment.onBackPress()) {
                return;
            }
        }
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

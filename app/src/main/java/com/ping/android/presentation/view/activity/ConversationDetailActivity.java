package com.ping.android.presentation.view.activity;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import com.ping.android.R;
import com.ping.android.model.User;
import com.ping.android.model.enums.Color;
import com.ping.android.presentation.view.fragment.BaseFragment;
import com.ping.android.presentation.view.fragment.ConversationGroupDetailFragment;
import com.ping.android.presentation.view.fragment.ConversationPVPDetailFragment;
import com.ping.android.utils.Navigator;
import com.ping.android.utils.ThemeUtils;
import com.ping.android.utils.configs.Constant;

import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;

public class ConversationDetailActivity extends CoreActivity implements HasSupportFragmentInjector {
    public static final String CONVERSATION_KEY = "CONVERSATION_KEY";
    public static final String CONVERSATION_TYPE_KEY = "CONVERSATION_TYPE_KEY";
    public static final String EXTRA_IMAGE_KEY = "EXTRA_IMAGE_KEY";

    @Inject
    Navigator navigator;
    @Inject
    DispatchingAndroidInjector<Fragment> fragmentInjector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidInjection.inject(this);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey(ChatActivity.EXTRA_CONVERSATION_COLOR)) {
                int color = bundle.getInt(ChatActivity.EXTRA_CONVERSATION_COLOR);
                Color currentColor = Color.from(color);
                ThemeUtils.onActivityCreateSetTheme(this, currentColor);
                String conversationName = bundle.getString(ChatActivity.EXTRA_CONVERSATION_NAME);
                List<User> inConversationUsers = bundle.getParcelableArrayList(ChatActivity.USERS_IN_GROUP);
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
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return fragmentInjector;
    }
}

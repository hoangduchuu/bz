package com.ping.android.presentation.view.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ping.android.R;
import com.ping.android.model.Call;
import com.ping.android.model.enums.Color;
import com.ping.android.presentation.presenters.MainPresenter;
import com.ping.android.presentation.view.fragment.CallFragment;
import com.ping.android.presentation.view.fragment.ContactFragment;
import com.ping.android.presentation.view.fragment.ConversationFragment;
import com.ping.android.presentation.view.fragment.GroupFragment;
import com.ping.android.presentation.view.fragment.ProfileFragment;
import com.ping.android.utils.BadgeHelper;
import com.ping.android.utils.KeyboardHelpers;
import com.ping.android.utils.UsersUtils;
import com.ping.android.utils.configs.Constant;
import com.quickblox.messages.services.SubscribeService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;

public class MainActivity extends CoreActivity implements HasSupportFragmentInjector, MainPresenter.View {

    SharedPreferences prefs;
    SharedPreferences.OnSharedPreferenceChangeListener listener;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private BadgeHelper badgeHelper;

    @Inject
    public MainPresenter presenter;
    @Inject
    DispatchingAndroidInjector<Fragment> fragmentInjector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidInjection.inject(this);
        presenter.create();
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(ChatActivity.CONVERSATION_ID)) {
            presenter.handleNewConversation(extras.getString(ChatActivity.CONVERSATION_ID));
        }
        setContentView(R.layout.activity_main);

        init();
        observeBadgeNumber();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //startCallService(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle extras = intent.getExtras();
        if (extras != null && extras.containsKey(ChatActivity.CONVERSATION_ID)) {
            presenter.handleNewConversation(extras.getString(ChatActivity.CONVERSATION_ID));
        }
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void connectivityChanged(boolean availableNow) {
        if (availableNow) {
            presenter.onNetworkAvailable();
        }
    }

    @Override
    public MainPresenter getPresenter() {
        return presenter;
    }

    private void init() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        badgeHelper = new BadgeHelper(this);
        viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();
        onChangeTab();
    }

    public void onEditMode(Boolean isEditMode) {
        tabLayout.postDelayed(() -> {
            Transition slide = new Slide();
            TransitionManager.beginDelayedTransition(tabLayout, slide);
            if (isEditMode) {
                tabLayout.setVisibility(View.GONE);
            } else {
                tabLayout.setVisibility(View.VISIBLE);
            }
        }, 10);
    }

    private void observeBadgeNumber() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int messageCount = prefs.getInt(Constant.PREFS_KEY_MESSAGE_COUNT, 0);
        updateMessageCount(messageCount);
        listener = (prefs, key) -> {
            if (key.equals(Constant.PREFS_KEY_MESSAGE_COUNT)) {
                int messageCount1 = prefs.getInt(key, 0);
                updateMessageCount(messageCount1);
            } else if (key.equals(Constant.PREFS_KEY_MISSED_CALL_COUNT)) {
                int count = prefs.getInt(key, 0);
                updateMissedCallCount(count);
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(listener);
    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setCustomView(getTabIcon(0, true));
        tabLayout.getTabAt(1).setCustomView(getTabIcon(1, false));
        tabLayout.getTabAt(2).setCustomView(getTabIcon(2, false));
        tabLayout.getTabAt(3).setCustomView(getTabIcon(3, false));
        tabLayout.getTabAt(4).setCustomView(getTabIcon(4, false));
    }

    private void onChangeTab() {
        tabLayout.setOnTabSelectedListener(new
           TabLayout.OnTabSelectedListener() {

               @Override
               public void onTabSelected(TabLayout.Tab tab) {
                   invalidateTabs(tab.getPosition());
               }

               @Override
               public void onTabUnselected(TabLayout.Tab tab) {
               }

               @Override
               public void onTabReselected(TabLayout.Tab tab) {
               }
           });
    }

    private void invalidateTabs(int selected) {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            tabLayout.getTabAt(i).getCustomView().findViewById(R.id.tab_item_icon).setSelected(i == selected);
        }
        int messageCount1 = prefs.getInt(Constant.PREFS_KEY_MESSAGE_COUNT, 0);
        updateMessageCount(messageCount1);
        if (selected == 1) {
            resetMissedCall();
        } else {
            int count = prefs.getInt(Constant.PREFS_KEY_MISSED_CALL_COUNT, 0);
            updateMissedCallCount(count);
        }
    }

    private Fragment getCurrentFragment() {
        return viewPagerAdapter.getItem(viewPager.getCurrentItem());
    }

    private void resetMissedCall() {
        // Reset missed count
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(Constant.PREFS_KEY_MISSED_CALL_COUNT, 0);
        editor.putLong(Constant.PREFS_KEY_MISSED_CALL_TIMESTAMP, System.currentTimeMillis());
        editor.apply();
        presenter.removeMissedCallsBadge();
        badgeHelper.clearMissedCall();
    }

    public void callAdded(Call call) {
        Fragment fragment = getCurrentFragment();
        if (fragment instanceof CallFragment) {
            resetMissedCall();
        } else {
            long lastTimestamp = prefs.getLong(Constant.PREFS_KEY_MISSED_CALL_TIMESTAMP, 0);
            if (call.status == Constant.CALL_STATUS_MISS
                    && (lastTimestamp == 0 || call.timestamp * 1000 > lastTimestamp)) {
                int currentMissed = prefs.getInt(Constant.PREFS_KEY_MISSED_CALL_COUNT, 0);
                prefs.edit().putInt(Constant.PREFS_KEY_MISSED_CALL_COUNT, currentMissed + 1).apply();
            }
        }
    }

    private View getTabIcon(int position, boolean selected) {
        int iconID = 0;
        View v;
        switch (position) {
            case 0:
                iconID = R.drawable.ic_home_message_selector;
                break;
            case 1:
                iconID = R.drawable.ic_home_call_selector;
                break;
            case 2:
                iconID = R.drawable.ic_home_group_selector;
                break;
            case 3:
                iconID = R.drawable.ic_home_friend_selector;
                break;
            case 4:
                iconID = R.drawable.ic_home_profile_selector;
                break;
        }

        ImageView iconView;
        v = LayoutInflater.from(this).inflate(R.layout.tab_layout_message, null);
        iconView = v.findViewById(R.id.tab_item_icon);
        iconView.setImageResource(iconID);
        iconView.setSelected(selected);

        return v;
    }

    private void updateMessageCount(int messageCount) {
        updateBadge(0, messageCount);
        if (messageCount == 0) {
            badgeHelper.resetConversationCount();
        }
    }

    private void updateMissedCallCount(int count) {
        updateBadge(1, count);
    }

    private void updateBadge(int index, int count) {
        View v = tabLayout.getTabAt(index).getCustomView();
        TextView tvMessageCount = v.findViewById(R.id.tab_item_number);
        if (count == 0) {
            tvMessageCount.setVisibility(View.GONE);
        } else {
            if (count <= 99) {
                tvMessageCount.setText("" + count);
                tvMessageCount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
            } else {
                tvMessageCount.setText("99+");
                tvMessageCount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 6);
            }
            tvMessageCount.setVisibility(View.VISIBLE);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        ProfileFragment profileFragment = new ProfileFragment();
        ContactFragment contactFragment = new ContactFragment();
        ConversationFragment conversationFragment = new ConversationFragment();
        GroupFragment groupFragment = new GroupFragment();
        CallFragment callFragment = new CallFragment();
        viewPagerAdapter.addFrag(conversationFragment, "Message");
        viewPagerAdapter.addFrag(callFragment, "Call");
        viewPagerAdapter.addFrag(groupFragment, "Group");
        viewPagerAdapter.addFrag(contactFragment, "Contact");
        viewPagerAdapter.addFrag(profileFragment, "Profile");
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(5);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                KeyboardHelpers.hideSoftInputKeyboard(MainActivity.this);
            }
        });
    }

    @Override
    public void openPhoneRequireView() {
        startActivity(new Intent(MainActivity.this, PhoneActivity.class));
    }

    @Override
    public void showMappingConfirm() {
        String message = "Your Transphabet was randomized using Emoji. Do you want to manually make changes to it?";
        new AlertDialog.Builder(this)
                .setTitle("NOTICE")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, whichButton) -> {
                    startActivity(new Intent(MainActivity.this, TransphabetActivity.class));
                    presenter.turnOffMappingConfirmation();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    presenter.turnOffMappingConfirmation();
                }).show();
    }

    @Override
    public void startCallService() {
        startCallService(this);
        SubscribeService.subscribeToPushes(this, false);
    }

    @Override
    public void moveToChatScreen(String conversationId, Color color) {
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        intent.putExtra(ChatActivity.CONVERSATION_ID, conversationId);
        intent.putExtra(ChatActivity.EXTRA_CONVERSATION_COLOR, color.getCode());
        startActivity(intent);
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return fragmentInjector;
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // return null to display only the icon
            return null;
        }
    }

}

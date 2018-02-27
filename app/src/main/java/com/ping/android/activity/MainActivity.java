package com.ping.android.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bzzzchat.cleanarchitecture.scopes.HasComponent;
import com.ping.android.dagger.loggedin.main.MainComponent;
import com.ping.android.dagger.loggedin.main.MainModule;
import com.ping.android.presentation.presenters.MainPresenter;
import com.ping.android.presentation.view.activity.ChatActivity;
import com.ping.android.presentation.view.fragment.CallFragment;
import com.ping.android.fragment.ContactFragment;
import com.ping.android.presentation.view.fragment.GroupFragment;
import com.ping.android.presentation.view.fragment.ConversationFragment;
import com.ping.android.presentation.view.fragment.ProfileFragment;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Call;
import com.ping.android.model.User;
import com.ping.android.service.BadgesHelper;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.BadgeHelper;
import com.ping.android.utils.UsersUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class MainActivity extends CoreActivity implements HasComponent<MainComponent> {

    SharedPreferences prefs;
    SharedPreferences.OnSharedPreferenceChangeListener listener;
    private User currentUser;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private BadgeHelper badgeHelper;

    @Inject
    public MainPresenter presenter;
    private MainComponent component;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getComponent().inject(this);
        presenter.create();
        String conversationId = getIntent().getStringExtra(ChatActivity.CONVERSATION_ID);
        if (StringUtils.isNotEmpty(conversationId)){
            Intent intent1 = new Intent(MainActivity.this, ChatActivity.class);
            intent1.putExtra(ChatActivity.CONVERSATION_ID, conversationId);
            startActivity(intent1);
        }
        setContentView(R.layout.activity_main);
        currentUser = UserManager.getInstance().getUser();

        init();
        observeBadgeNumber();
        UserManager.getInstance().addValueEventListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UserManager.getInstance().removeValueEventListener();
    }
    @Override
    public void onBackPressed() {
        return;
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

        if (currentUser != null && TextUtils.isEmpty(currentUser.phone)) {
            startActivity(new Intent(MainActivity.this, PhoneActivity.class));
        }
        if (currentUser != null && !currentUser.showMappingConfirm) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("NOTICE")
                    .setMessage("Mask your messages by replacing the Alphabet with your own characters. Do you want to manually make changes to the Alphabet?")
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            startActivity(new Intent(MainActivity.this, TransphabetActivity.class));
                            ServiceManager.getInstance().updateShowMappingConfirm(true);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            UsersUtils.randomizeTransphabet();
                            ServiceManager.getInstance().updateShowMappingConfirm(true);
                        }
                    }).show();
        }
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
            tabLayout.getTabAt(i).setCustomView(null);
            tabLayout.getTabAt(i).setCustomView(getTabIcon(i, i == selected));
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
        BadgesHelper.getInstance().removeCurrentUserBadges("missed_call");
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
        int colorID = selected ? R.color.orange : R.color.black_transparent_50;
        String title = "Messages";
        View v;
        switch (position) {
            case 0:
                iconID = selected ? R.drawable.ic_tab_messages_orange : R.drawable.ic_tab_messages_gray;
                title = "Messages";
                break;
            case 1:
                iconID = selected ? R.drawable.ic_tab_calls_orange : R.drawable.ic_tab_calls_gray;
                title = "Calls";
                break;
            case 2:
                iconID = selected ? R.drawable.ic_tab_group_orange : R.drawable.ic_tab_group_gray;
                title = "Groups";
                break;
            case 3:
                iconID = selected ? R.drawable.ic_tab_contacts_orange : R.drawable.ic_tab_contacts_gray;
                title = "Contacts";
                break;
            case 4:
                iconID = selected ? R.drawable.ic_tab_profile_orange : R.drawable.ic_tab_profile_gray;
                title = "Profile";
                break;
        }

        ImageView iconView;
        TextView numberView, titleView;
        v = LayoutInflater.from(this).inflate(R.layout.tab_layout_message, null);
        numberView = (TextView) v.findViewById(R.id.tab_item_number);


        iconView = (ImageView) v.findViewById(R.id.tab_item_icon);
        iconView.setImageResource(iconID);
        titleView = (TextView) v.findViewById(R.id.tab_item_title);
        titleView.setText(title);
        titleView.setTextColor(getResources().getColor(colorID));

        return v;
    }

    private void updateMessageCount(int messageCount) {
        updateBadge(0, messageCount);
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
    }

    @Override
    public MainComponent getComponent() {
        if (component == null) {
            component = getLoggedInComponent().provideMainComponent(new MainModule());
        }
        return component;
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

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
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ping.android.fragment.CallFragment;
import com.ping.android.fragment.ContactFragment;
import com.ping.android.fragment.GroupFragment;
import com.ping.android.fragment.MessageFragment;
import com.ping.android.fragment.ProfileFragment;
import com.ping.android.managers.UserManager;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.UsersUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends CoreActivity {

    SharedPreferences prefs;
    SharedPreferences.OnSharedPreferenceChangeListener listener;
    private User currentUser;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentUser = UserManager.getInstance().getUser();

        init();
        observeBadgeNumber();
    }


    @Override
    public void onBackPressed() {
        return;
    }

    private void init() {
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();
        onChangeTab();

        if (currentUser != null && (StringUtils.isBlank(currentUser.phone) || StringUtils.isEmpty(currentUser.phone))) {
            startActivity(new Intent(MainActivity.this, PhoneActivity.class));
        }
        if (currentUser != null && (currentUser.showMappingConfirm == null || !currentUser.showMappingConfirm)) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("NOTICE")
                    .setMessage("Mask your messages by replacing the Alphabet with your own characters. Do you want to manually make changes to the Alphabet?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            startActivity(new Intent(MainActivity.this, TransphabetActivity.class));
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            UsersUtils.randomizeTransphabet();
                        }
                    }).show();
            ServiceManager.getInstance().updateShowMappingConfirm(true);
        }
    }

    public void onEditMode(Boolean isEditMode) {
        tabLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                Transition slide = new Slide();
                TransitionManager.beginDelayedTransition(tabLayout, slide);
                if (isEditMode) {
                    tabLayout.setVisibility(View.GONE);
                } else {
                    tabLayout.setVisibility(View.VISIBLE);
                }
            }
        }, 10);
    }

    private void observeBadgeNumber() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int messageCount = prefs.getInt(Constant.PREFS_KEY_MESSAGE_COUNT, 0);
        updateMessageCount(messageCount);
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (key.equals(Constant.PREFS_KEY_MESSAGE_COUNT)) {
                    int messageCount = prefs.getInt(key, 0);
                    updateMessageCount(messageCount);
                }
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(listener);

    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setCustomView(getTabIcon(0, false));
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
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int messageCount = prefs.getInt(Constant.PREFS_KEY_MESSAGE_COUNT, 0);
        updateMessageCount(messageCount);
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

//        if (numberView != null)
//            numberView.setText('1');

        iconView = (ImageView) v.findViewById(R.id.tab_item_icon);
        iconView.setImageResource(iconID);
        titleView = (TextView) v.findViewById(R.id.tab_item_title);
        titleView.setText(title);
        titleView.setTextColor(getResources().getColor(colorID));

        return v;
    }

    private void updateMessageCount(int messageCount) {
        View v = tabLayout.getTabAt(0).getCustomView();
        TextView tvMessageCount = (TextView) v.findViewById(R.id.tab_item_number);
        if (messageCount == 0) {
            tvMessageCount.setVisibility(View.GONE);
        } else {
            tvMessageCount.setText("" + messageCount);
            tvMessageCount.setVisibility(View.VISIBLE);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        ProfileFragment profileFragment = new ProfileFragment();
        ContactFragment contactFragment = new ContactFragment();
        MessageFragment messageFragment = new MessageFragment();
        GroupFragment groupFragment = new GroupFragment();
        CallFragment callFragment = new CallFragment();
        adapter.addFrag(messageFragment, "Message");
        adapter.addFrag(callFragment, "Call");
        adapter.addFrag(groupFragment, "Group");
        adapter.addFrag(contactFragment, "Contact");
        adapter.addFrag(profileFragment, "Profile");
        viewPager.setAdapter(adapter);
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

package com.ping.android.presentation.view.activity;

import android.os.Bundle;
import androidx.annotation.ColorRes;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.view.Window;
import android.view.WindowManager;

import com.ping.android.R;
import com.ping.android.presentation.view.fragment.BaseFragment;
import com.ping.android.presentation.view.fragment.TransphabetListFragment;
import com.ping.android.utils.Navigator;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;

public class TransphabetActivity extends CoreActivity implements HasSupportFragmentInjector {
    @Inject
    Navigator navigator;
    @Inject
    DispatchingAndroidInjector<Fragment> fragmentInjector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transphabet);
        AndroidInjection.inject(this);
        navigator.setNavigationListener(() -> {
            BaseFragment fragment = navigator.getCurrentFragment();
//            if (fragment instanceof MappingFragment) {
//                updateStatusBarColor(R.color.colorPrimaryBlack);
//            } else {
//                updateStatusBarColor(R.color.orange);
//            }
        });
        navigator.init(getSupportFragmentManager(), R.id.transphabet_container);
        if (savedInstanceState == null) {
            navigator.openAsRoot(TransphabetListFragment.newInstance());
        }
    }

    @Override
    public void onBackPressed() {
        navigator.navigateBack(this);
    }

    public Navigator getNavigator() {
        return navigator;
    }

    public void updateStatusBarColor(@ColorRes int color) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, color));
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return fragmentInjector;
    }
}

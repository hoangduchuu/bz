package com.ping.android.presentation.view.activity;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.view.Window;
import android.view.WindowManager;

import com.bzzzchat.cleanarchitecture.scopes.HasComponent;
import com.ping.android.R;
import com.ping.android.dagger.loggedin.transphabet.TransphabetComponent;
import com.ping.android.presentation.view.fragment.BaseFragment;
import com.ping.android.presentation.view.fragment.MappingFragment;
import com.ping.android.presentation.view.fragment.TransphabetFragment;
import com.ping.android.utils.Navigator;

public class TransphabetActivity extends CoreActivity implements HasComponent<TransphabetComponent> {

    private Navigator navigator;
    TransphabetComponent component;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transphabet);

        navigator = new Navigator();
        navigator.setNavigationListener(() -> {
            BaseFragment fragment = navigator.getCurrentFragment();
            if (fragment instanceof MappingFragment) {
                updateStatusBarColor(R.color.colorPrimaryBlack);
            } else {
                updateStatusBarColor(R.color.orange);
            }
        });
        navigator.init(getSupportFragmentManager(), R.id.transphabet_container);
        if (savedInstanceState == null) {
            navigator.openAsRoot(TransphabetFragment.newInstance());
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
    public TransphabetComponent getComponent() {
        if (component == null) {
            component = getLoggedInComponent().provideTransphabetComponent();
        }
        return component;
    }
}

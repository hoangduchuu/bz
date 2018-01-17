package com.ping.android.activity;

import android.os.Bundle;

import com.ping.android.fragment.transphabet.TransphabetFragment;
import com.ping.android.utils.Navigator;

public class TransphabetActivity extends CoreActivity {

    private Navigator navigator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transphabet);

        navigator = new Navigator();
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
}

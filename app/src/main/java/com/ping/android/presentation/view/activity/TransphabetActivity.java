package com.ping.android.presentation.view.activity;

import android.os.Bundle;

import com.bzzzchat.cleanarchitecture.scopes.HasComponent;
import com.ping.android.activity.R;
import com.ping.android.dagger.loggedin.transphabet.TransphabetComponent;
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

    @Override
    public TransphabetComponent getComponent() {
        if (component == null) {
            component = getLoggedInComponent().provideTransphabetComponent();
        }
        return component;
    }
}

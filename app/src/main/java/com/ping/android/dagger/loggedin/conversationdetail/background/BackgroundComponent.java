package com.ping.android.dagger.loggedin.conversationdetail.background;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.view.fragment.BackgroundFragment;

import dagger.Subcomponent;

@PerFragment
@Subcomponent(modules = BackgroundModule.class)
public interface BackgroundComponent {
    void inject(BackgroundFragment fragment);
}

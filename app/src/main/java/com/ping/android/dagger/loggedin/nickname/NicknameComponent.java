package com.ping.android.dagger.loggedin.nickname;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.view.activity.NicknameActivity;

import dagger.Subcomponent;

/**
 * Created by tuanluong on 1/25/18.
 */
@PerActivity
@Subcomponent(modules = { NicknameModule.class })
public interface NicknameComponent {
    void inject(NicknameActivity activity);
}

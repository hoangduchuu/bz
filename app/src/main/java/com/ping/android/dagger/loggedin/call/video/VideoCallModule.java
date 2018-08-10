package com.ping.android.dagger.loggedin.call.video;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.presenters.VideoCallPresenter;
import com.ping.android.presentation.presenters.impl.VideoCallPresenterImpl;
import com.ping.android.presentation.view.fragment.VideoConversationFragment;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 3/20/18.
 */
@Module
public abstract class VideoCallModule {
    @Binds
    public abstract VideoCallPresenter.View provideView(VideoConversationFragment fragment);

    @Provides
    @PerFragment
    public static VideoCallPresenter providePresenter(VideoCallPresenterImpl presenter) {
        return presenter;
    }
}

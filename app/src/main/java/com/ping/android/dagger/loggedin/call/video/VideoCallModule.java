package com.ping.android.dagger.loggedin.call.video;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.presenters.AudioCallPresenter;
import com.ping.android.presentation.presenters.VideoCallPresenter;
import com.ping.android.presentation.presenters.impl.AudioCallPresenterImpl;
import com.ping.android.presentation.presenters.impl.VideoCallPresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 3/20/18.
 */
@Module
public class VideoCallModule {
    private final VideoCallPresenter.View view;

    public VideoCallModule(VideoCallPresenter.View view) {
        this.view = view;
    }

    @Provides
    @PerFragment
    public VideoCallPresenter.View provideView() {
        return view;
    }

    @Provides
    @PerFragment
    public VideoCallPresenter providePresenter(VideoCallPresenterImpl presenter) {
        return presenter;
    }
}

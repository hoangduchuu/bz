package com.ping.android.dagger.loggedin.gallery

import com.ping.android.dagger.scopes.PerActivity
import com.ping.android.presentation.presenters.GalleryPresenter
import com.ping.android.presentation.presenters.GalleryPresenterImpl
import com.ping.android.presentation.view.activity.GalleryActivity
import com.ping.android.utils.Navigator
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
abstract class GalleryModule {
    @Binds
    abstract fun provideView(activity: GalleryActivity): GalleryPresenter.View

    @Module
    companion object {
        @JvmStatic
        @Provides
        @PerActivity
        fun providePresenter(presenter: GalleryPresenterImpl): GalleryPresenter {
            return presenter
        }

        @JvmStatic
        @Provides
        @PerActivity
        fun provideNavigator(): Navigator {
            return Navigator()
        }
    }
}
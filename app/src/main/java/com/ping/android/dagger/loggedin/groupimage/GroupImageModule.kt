package com.ping.android.dagger.loggedin.groupimage

import com.ping.android.dagger.scopes.PerActivity
import com.ping.android.presentation.presenters.GroupImageGalleryPresenter
import com.ping.android.presentation.presenters.GroupImageGalleryPresenterImpl
import com.ping.android.presentation.view.activity.GroupImageGalleryActivity
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
abstract class GroupImageModule {
    @Binds
    abstract fun provideView(activity: GroupImageGalleryActivity): GroupImageGalleryPresenter.View

    @Module
    companion object {
        @JvmStatic
        @Provides
        @PerActivity
        fun provideGroupImageGalleryPresenter(presenter: GroupImageGalleryPresenterImpl): GroupImageGalleryPresenter {
            return presenter
        }
    }
}
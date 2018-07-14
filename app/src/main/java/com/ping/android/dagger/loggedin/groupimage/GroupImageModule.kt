package com.ping.android.dagger.loggedin.groupimage

import com.ping.android.dagger.scopes.PerActivity
import com.ping.android.presentation.presenters.GroupImageGalleryPresenter
import com.ping.android.presentation.presenters.GroupImageGalleryPresenterImpl
import dagger.Module
import dagger.Provides

@Module
class GroupImageModule {
    @Provides
    @PerActivity
    fun provideGroupImageGalleryPresenter(presenter: GroupImageGalleryPresenterImpl): GroupImageGalleryPresenter {
        return presenter
    }
}
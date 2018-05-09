package com.ping.android.dagger.loggedin.conversationdetail.gallery

import com.ping.android.dagger.scopes.PerFragment
import com.ping.android.presentation.presenters.GalleryPresenter
import com.ping.android.presentation.presenters.GalleryPresenterImpl
import dagger.Module
import dagger.Provides

@Module
class GalleryModule(var view: GalleryPresenter.View) {
    @Provides
    @PerFragment
    fun provideView(): GalleryPresenter.View {
        return view
    }

    @Provides
    @PerFragment
    fun providePresenter(presenter: GalleryPresenterImpl): GalleryPresenter {
        return presenter
    }
}
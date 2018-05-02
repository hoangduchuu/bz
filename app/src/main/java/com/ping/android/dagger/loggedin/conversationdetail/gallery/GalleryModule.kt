package com.ping.android.dagger.loggedin.conversationdetail.gallery

import com.ping.android.dagger.scopes.PerActivity
import com.ping.android.dagger.scopes.PerFragment
import com.ping.android.model.Message
import com.ping.android.presentation.presenters.GalleryPresenter
import com.ping.android.presentation.presenters.GalleryPresenterImpl
import com.ping.android.utils.Navigator
import dagger.Module
import dagger.Provides

@Module
class GalleryModule(var view: GalleryPresenter.View) {
    @Provides
    @PerActivity
    fun provideView(): GalleryPresenter.View {
        return view
    }

    @Provides
    @PerActivity
    fun providePresenter(presenter: GalleryPresenterImpl): GalleryPresenter {
        return presenter
    }

    @Provides
    @PerActivity
    fun provideNavigator(): Navigator {
        return Navigator()
    }
}
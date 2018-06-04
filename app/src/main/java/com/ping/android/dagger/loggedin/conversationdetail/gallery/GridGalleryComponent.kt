package com.ping.android.dagger.loggedin.conversationdetail.gallery

import com.ping.android.dagger.scopes.PerFragment
import com.ping.android.presentation.presenters.GalleryPresenter
import com.ping.android.presentation.view.fragment.GridGalleryFragment
import com.ping.android.presentation.view.fragment.ViewPagerGalleryFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Module
class GridGalleryModule() {

}

@PerFragment
@Subcomponent(modules = [GridGalleryModule::class])
interface GridGalleryComponent {
    fun inject(fragment: GridGalleryFragment)
    fun inject(fragment: ViewPagerGalleryFragment)
}
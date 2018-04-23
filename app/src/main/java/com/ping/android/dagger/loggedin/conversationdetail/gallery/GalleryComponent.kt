package com.ping.android.dagger.loggedin.conversationdetail.gallery

import com.ping.android.dagger.scopes.PerActivity
import com.ping.android.presentation.view.activity.GalleryActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [ GalleryModule::class ])
interface GalleryComponent {
    fun inject(activity: GalleryActivity)

    fun provideGridGalleryComponent(gridGalleryModule: GridGalleryModule): GridGalleryComponent
}
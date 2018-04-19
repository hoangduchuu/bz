package com.ping.android.dagger.loggedin.conversationdetail.gallery

import com.ping.android.dagger.scopes.PerFragment
import com.ping.android.presentation.view.fragment.GridGalleryFragment
import dagger.Subcomponent

@PerFragment
@Subcomponent(modules = [ GalleryModule::class ])
interface GalleryComponent {
    fun inject(fragment: GridGalleryFragment)
}
package com.ping.android.dagger.loggedin.groupimage

import com.ping.android.dagger.scopes.PerActivity
import com.ping.android.presentation.view.activity.GroupImageGalleryActivity
import dagger.Subcomponent

@Subcomponent
@PerActivity
interface GroupImageComponent {
    fun inject(activity: GroupImageGalleryActivity)
}

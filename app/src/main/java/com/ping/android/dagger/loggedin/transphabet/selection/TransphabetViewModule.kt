package com.ping.android.dagger.loggedin.transphabet.selection

import com.ping.android.presentation.presenters.TransphabetPresenter
import com.ping.android.presentation.view.fragment.SelectiveCategoriesFragment
import com.ping.android.presentation.view.fragment.TransphabetListFragment
import dagger.Binds
import dagger.Module

@Module
abstract class TransphabetListViewModule {
    @Binds
    abstract fun provideView(fragment: TransphabetListFragment): TransphabetPresenter.View
}

@Module
abstract class SelectionViewModule {
    @Binds
    abstract fun provideView(fragment: SelectiveCategoriesFragment): TransphabetPresenter.View
}
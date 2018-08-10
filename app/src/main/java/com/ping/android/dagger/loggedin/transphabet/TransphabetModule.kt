package com.ping.android.dagger.loggedin.transphabet

import com.ping.android.dagger.loggedin.transphabet.manualmapping.ManualMappingModule
import com.ping.android.dagger.loggedin.transphabet.selection.SelectionViewModule
import com.ping.android.dagger.loggedin.transphabet.selection.TransphabetListViewModule
import com.ping.android.dagger.loggedin.transphabet.selection.TransphabetSelectionModule
import com.ping.android.dagger.scopes.PerActivity
import com.ping.android.dagger.scopes.PerFragment
import com.ping.android.presentation.view.fragment.MappingFragment
import com.ping.android.presentation.view.fragment.SelectiveCategoriesFragment
import com.ping.android.presentation.view.fragment.TransphabetListFragment
import com.ping.android.utils.Navigator
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module
abstract class TransphabetModule {
    @PerFragment
    @ContributesAndroidInjector(modules = [ ManualMappingModule::class ])
    abstract fun bindMappingFragment() : MappingFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [ TransphabetSelectionModule::class, TransphabetListViewModule::class ])
    abstract fun bindTransphabetListFragment() : TransphabetListFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [ TransphabetSelectionModule::class, SelectionViewModule::class ])
    abstract fun bindSelectiveCategoriesFragmentt() : SelectiveCategoriesFragment

    @Module
    companion object {
        @JvmStatic
        @Provides
        @PerActivity
        fun provideNavigator() = Navigator()
    }
}
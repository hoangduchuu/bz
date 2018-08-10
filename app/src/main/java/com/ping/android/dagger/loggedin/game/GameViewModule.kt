package com.ping.android.dagger.loggedin.game

import com.ping.android.presentation.presenters.GamePresenter
import com.ping.android.presentation.view.activity.GameMemoryActivity
import com.ping.android.presentation.view.activity.GamePuzzleActivity
import com.ping.android.presentation.view.activity.GameTicTacToeActivity
import dagger.Binds
import dagger.Module

@Module
abstract class PuzzleViewModule {
    @Binds
    abstract fun provideView(activity: GamePuzzleActivity): GamePresenter.View
}

@Module
abstract class MemoryViewModule {
    @Binds
    abstract fun provideView(activity: GameMemoryActivity): GamePresenter.View
}

@Module
abstract class TicTacToeViewModule {
    @Binds
    abstract fun provideView(activity: GameTicTacToeActivity): GamePresenter.View
}
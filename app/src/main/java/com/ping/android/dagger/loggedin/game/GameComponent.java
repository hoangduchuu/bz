package com.ping.android.dagger.loggedin.game;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.view.activity.GameActivity;
import com.ping.android.presentation.view.activity.GameMemoryActivity;
import com.ping.android.presentation.view.activity.GameTicTacToeActivity;
import com.ping.android.presentation.view.activity.PuzzleActivity;

import dagger.Subcomponent;

/**
 * Created by tuanluong on 1/25/18.
 */
@PerActivity
@Subcomponent(modules = {GameModule.class})
public interface GameComponent {
    void inject(GameActivity activity);

    void inject(PuzzleActivity activity);

    void inject(GameMemoryActivity activity);

    void inject(GameTicTacToeActivity gameTicTacToeActivity);
}

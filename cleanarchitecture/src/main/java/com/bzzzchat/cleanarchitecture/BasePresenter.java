package com.bzzzchat.cleanarchitecture;

/**
 * Created by tuanluong on 1/23/18.
 */

public interface BasePresenter {
    default void create() {}
    default void resume() {}
    default void pause() {}
    default void destroy() {}
}

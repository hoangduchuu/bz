package com.tl.extensions

import android.support.v4.app.Fragment
import com.tl.cleanarchitecture.scopes.HasComponent

/**
 * Created by tuanluong on 10/31/17.
 */

fun <C> Fragment.getComponent(componentType: Class<C>): C {
    if (activity is HasComponent<*>) {
        return componentType.cast((activity as com.tl.cleanarchitecture.scopes.HasComponent<C>).component)
    }
    throw IllegalStateException("activity must be implement HasComponent")
}
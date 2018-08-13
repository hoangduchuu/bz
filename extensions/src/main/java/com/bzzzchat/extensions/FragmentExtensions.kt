package com.bzzzchat.extensions

import androidx.fragment.app.Fragment
import com.bzzzchat.cleanarchitecture.scopes.HasComponent

/**
 * Created by tuanluong on 10/31/17.
 */

fun <C> androidx.fragment.app.Fragment.getComponent(componentType: Class<C>): C {
    if (activity is HasComponent<*>) {
        return componentType.cast((activity as com.bzzzchat.cleanarchitecture.scopes.HasComponent<C>).component)
    }
    throw IllegalStateException("activity must be implement HasComponent")
}
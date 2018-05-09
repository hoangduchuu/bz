package com.bzzzchat.cleanarchitecture.scopes

/**
 * Detect whether the parent has the C component
 */
interface HasComponent<C> {
    val component: C
}
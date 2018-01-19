package com.tl.cleanarchitecture.scopes

import javax.inject.Qualifier
import javax.inject.Scope

/**
 * Created by tuanluong on 10/30/17.
 */

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
@Scope
annotation class PerActivity
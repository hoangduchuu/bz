package com.ping.android.dagger.scopes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

/**
 * Created by tuanluong on 1/24/18.
 */
@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface LoggedOut {
}

package com.ping.android.dagger.loggedin;

import com.ping.android.dagger.scopes.LoggedIn;
import com.ping.android.data.repository.SearchRepositoryImpl;
import com.ping.android.domain.repository.SearchRepository;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 1/23/18.
 */
@Module
public class RepositoryModule {
    @Provides
    @LoggedIn
    public SearchRepository provideSearchRepository(SearchRepositoryImpl repository) {
        return repository;
    }
}

package com.ping.android.dagger.loggedin;

import com.ping.android.dagger.scopes.LoggedIn;
import com.ping.android.data.repository.CommonRepositoryImpl;
import com.ping.android.data.repository.ConversationRepositoryImpl;
import com.ping.android.data.repository.GroupRepositoryImpl;
import com.ping.android.data.repository.SearchRepositoryImpl;
import com.ping.android.data.repository.StorageRepositoryImpl;
import com.ping.android.data.repository.UserRepositoryImpl;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.GroupRepository;
import com.ping.android.domain.repository.SearchRepository;
import com.ping.android.domain.repository.StorageRepository;
import com.ping.android.domain.repository.UserRepository;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 1/23/18.
 */
@Module
public class RepositoryModule {
    @Provides
    @LoggedIn
    public CommonRepository provideCommonRepository(CommonRepositoryImpl repository) {
        return repository;
    }

    @Provides
    @LoggedIn
    public SearchRepository provideSearchRepository(SearchRepositoryImpl repository) {
        return repository;
    }

    @Provides
    @LoggedIn
    public ConversationRepository provideConversationRepository(ConversationRepositoryImpl repository) {
        return repository;
    }

    @Provides
    @LoggedIn
    public UserRepository provideUserRepository(UserRepositoryImpl repository) {
        return repository;
    }

    @Provides
    @LoggedIn
    public GroupRepository provideGroupRepository(GroupRepositoryImpl repository) {
        return repository;
    }

    @Provides
    @LoggedIn
    public StorageRepository provideStorageRepository(StorageRepositoryImpl repository) {
        return repository;
    }
}

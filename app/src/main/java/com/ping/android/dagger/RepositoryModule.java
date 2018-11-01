package com.ping.android.dagger;

import android.app.Application;

import com.ping.android.data.repository.CommonRepositoryImpl;
import com.ping.android.data.repository.ConversationRepositoryImpl;
import com.ping.android.data.repository.GroupRepositoryImpl;
import com.ping.android.data.repository.MessageRepositoryImpl;
import com.ping.android.data.repository.NotificationMessageRepositoryImpl;
import com.ping.android.data.repository.NotificationRepositoryImpl;
import com.ping.android.data.repository.QuickbloxRepositoryImpl;
import com.ping.android.data.repository.SearchRepositoryImpl;
import com.ping.android.data.repository.StorageRepositoryImpl;
import com.ping.android.data.repository.UserRepositoryImpl;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.GroupRepository;
import com.ping.android.domain.repository.MessageRepository;
import com.ping.android.domain.repository.NotificationMessageRepository;
import com.ping.android.domain.repository.NotificationRepository;
import com.ping.android.domain.repository.QuickbloxRepository;
import com.ping.android.domain.repository.SearchRepository;
import com.ping.android.domain.repository.StorageRepository;
import com.ping.android.domain.repository.UserRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 1/23/18.
 */
@Module
public class RepositoryModule {
    @Provides
    @Singleton
    public CommonRepository provideCommonRepository(CommonRepositoryImpl repository) {
        return repository;
    }

    @Provides
    @Singleton
    public SearchRepository provideSearchRepository(SearchRepositoryImpl repository) {
        return repository;
    }

    @Provides
    @Singleton
    public ConversationRepository provideConversationRepository(ConversationRepositoryImpl repository) {
        return repository;
    }

    @Provides
    @Singleton
    public UserRepository provideUserRepository(UserRepositoryImpl repository) {
        return repository;
    }

    @Provides
    @Singleton
    public GroupRepository provideGroupRepository(GroupRepositoryImpl repository) {
        return repository;
    }

    @Provides
    @Singleton
    public StorageRepository provideStorageRepository(StorageRepositoryImpl repository) {
        return repository;
    }

    @Provides
    @Singleton
    public QuickbloxRepository provideQuickbloxRepository(QuickbloxRepositoryImpl repository) {
        return repository;
    }

    @Provides
    @Singleton
    public MessageRepository provideMessageRepository(MessageRepositoryImpl repository) {
        return repository;
    }

    @Provides
    @Singleton
    public NotificationRepository provideNotificationRepository(NotificationRepositoryImpl repository) {
        return repository;
    }

    @Provides
    @Singleton
    public NotificationMessageRepository provideNotificationMessageRepository(Application application) {
        return new NotificationMessageRepositoryImpl(application);
    }
}

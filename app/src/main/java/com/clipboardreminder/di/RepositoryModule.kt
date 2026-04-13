package com.clipboardreminder.di

import com.clipboardreminder.data.repository.AppPreferencesRepositoryImpl
import com.clipboardreminder.data.repository.FieldRepositoryImpl
import com.clipboardreminder.data.repository.NotificationRepositoryImpl
import com.clipboardreminder.data.repository.ReminderRepositoryImpl
import com.clipboardreminder.domain.repository.AppPreferencesRepository
import com.clipboardreminder.domain.repository.FieldRepository
import com.clipboardreminder.domain.repository.NotificationRepository
import com.clipboardreminder.domain.repository.ReminderRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindFieldRepository(impl: FieldRepositoryImpl): FieldRepository

    @Binds
    abstract fun bindReminderRepository(impl: ReminderRepositoryImpl): ReminderRepository

    @Binds
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @Binds
    abstract fun bindAppPreferencesRepository(impl: AppPreferencesRepositoryImpl): AppPreferencesRepository
}

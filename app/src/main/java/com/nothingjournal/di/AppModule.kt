package com.nothingjournal.di

import android.content.Context
import androidx.room.Room
import com.nothingjournal.ai.LocalAiClient
import com.nothingjournal.ai.OnDeviceAiClient
import com.nothingjournal.data.local.EntryDao
import com.nothingjournal.data.local.JournalDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): JournalDatabase =
        Room.databaseBuilder(context, JournalDatabase::class.java, "journal.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideEntryDao(db: JournalDatabase): EntryDao = db.entryDao()

    @Provides
    @Singleton
    fun provideLocalAiClient(impl: OnDeviceAiClient): LocalAiClient = impl
}

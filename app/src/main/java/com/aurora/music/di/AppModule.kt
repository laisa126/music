package com.aurora.music.di

import android.content.Context
import androidx.room.Room
import com.aurora.music.core.common.AppDispatchers
import com.aurora.music.data.database.AuroraDatabase
import com.aurora.music.data.database.dao.CollectionStateDao
import com.aurora.music.data.database.dao.EqualizerPresetDao
import com.aurora.music.data.database.dao.HistoryDao
import com.aurora.music.data.database.dao.LyricsDao
import com.aurora.music.data.database.dao.PlaylistDao
import com.aurora.music.data.database.dao.QueueDao
import com.aurora.music.data.database.dao.RecentSearchDao
import com.aurora.music.data.database.dao.SongDao
import com.aurora.music.data.preferences.SettingsDataStore
import com.aurora.music.data.repository.LocalMusicRepository
import com.aurora.music.domain.repository.MusicRepository
import com.aurora.music.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AuroraDatabase =
        Room.databaseBuilder(context, AuroraDatabase::class.java, AuroraDatabase.NAME)
            // Explicit migrations only — never destructive in production.
            .addMigrations(*AuroraDatabase.MIGRATIONS)
            .build()

    @Provides fun provideSongDao(db: AuroraDatabase): SongDao = db.songDao()

    @Provides
    fun provideCollectionStateDao(db: AuroraDatabase): CollectionStateDao = db.collectionStateDao()

    @Provides fun providePlaylistDao(db: AuroraDatabase): PlaylistDao = db.playlistDao()

    @Provides fun provideHistoryDao(db: AuroraDatabase): HistoryDao = db.historyDao()

    @Provides fun provideLyricsDao(db: AuroraDatabase): LyricsDao = db.lyricsDao()

    @Provides
    fun provideEqualizerPresetDao(db: AuroraDatabase): EqualizerPresetDao =
        db.equalizerPresetDao()

    @Provides
    fun provideRecentSearchDao(db: AuroraDatabase): RecentSearchDao = db.recentSearchDao()

    @Provides fun provideQueueDao(db: AuroraDatabase): QueueDao = db.queueDao()
}

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Singleton
    fun provideDispatchers(): AppDispatchers = AppDispatchers()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Phase 1 binds the local implementation. Phase 2 swaps this single line
     * for a merged (local + catalog) repository — nothing else changes.
     */
    @Binds
    @Singleton
    abstract fun bindMusicRepository(impl: LocalMusicRepository): MusicRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsDataStore): SettingsRepository
}

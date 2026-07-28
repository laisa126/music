package com.aurora.music.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aurora.music.data.database.dao.CollectionStateDao
import com.aurora.music.data.database.dao.EqualizerPresetDao
import com.aurora.music.data.database.dao.HistoryDao
import com.aurora.music.data.database.dao.LyricsDao
import com.aurora.music.data.database.dao.PlaylistDao
import com.aurora.music.data.database.dao.QueueDao
import com.aurora.music.data.database.dao.RecentSearchDao
import com.aurora.music.data.database.dao.SongDao
import com.aurora.music.data.database.entity.AlbumStateEntity
import com.aurora.music.data.database.entity.ArtistStateEntity
import com.aurora.music.data.database.entity.EqualizerPresetEntity
import com.aurora.music.data.database.entity.FolderStateEntity
import com.aurora.music.data.database.entity.HistoryEntity
import com.aurora.music.data.database.entity.LyricsEntity
import com.aurora.music.data.database.entity.PlaylistEntity
import com.aurora.music.data.database.entity.PlaylistSongEntity
import com.aurora.music.data.database.entity.QueueEntity
import com.aurora.music.data.database.entity.RecentSearchEntity
import com.aurora.music.data.database.entity.SongEntity

@Database(
    entities = [
        SongEntity::class,
        AlbumStateEntity::class,
        ArtistStateEntity::class,
        FolderStateEntity::class,
        PlaylistEntity::class,
        PlaylistSongEntity::class,
        HistoryEntity::class,
        LyricsEntity::class,
        EqualizerPresetEntity::class,
        RecentSearchEntity::class,
        QueueEntity::class,
    ],
    version = AuroraDatabase.VERSION,
    exportSchema = true,
)
abstract class AuroraDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao
    abstract fun collectionStateDao(): CollectionStateDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun historyDao(): HistoryDao
    abstract fun lyricsDao(): LyricsDao
    abstract fun equalizerPresetDao(): EqualizerPresetDao
    abstract fun recentSearchDao(): RecentSearchDao
    abstract fun queueDao(): QueueDao

    companion object {
        const val VERSION = 1
        const val NAME = "aurora-music.db"

        /**
         * Explicit migrations only — never `fallbackToDestructiveMigration` in
         * release builds (spec Section 11 item 3). New migrations are appended
         * here and covered by an instrumentation test.
         */
        val MIGRATIONS: Array<Migration> = arrayOf(
            // Placeholder illustrating the required shape for v1 -> v2.
            object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    // No-op: schema v2 not yet defined. Kept so the migration
                    // path is wired and testable from day one.
                }
            },
        ).filter { it.startVersion < VERSION }.toTypedArray()
    }
}

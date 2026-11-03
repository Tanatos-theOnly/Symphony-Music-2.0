package com.tanatos.symphonymusic2.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.tanatos.symphonymusic2.model.Playlist
import com.tanatos.symphonymusic2.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class SymphonyDatabase private constructor(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val _likedTracksFlow = MutableStateFlow<List<Track>>(emptyList())
    val likedTracksFlow: StateFlow<List<Track>> = _likedTracksFlow.asStateFlow()

    private val _downloadedTracksFlow = MutableStateFlow<List<Track>>(emptyList())
    val downloadedTracksFlow: StateFlow<List<Track>> = _downloadedTracksFlow.asStateFlow()

    private val _playlistsFlow = MutableStateFlow<List<Playlist>>(emptyList())
    val playlistsFlow: StateFlow<List<Playlist>> = _playlistsFlow.asStateFlow()

    private val _recentTracksFlow = MutableStateFlow<List<Track>>(emptyList())
    val recentTracksFlow: StateFlow<List<Track>> = _recentTracksFlow.asStateFlow()

    init {
        refreshAll()
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE liked_tracks (
                id TEXT PRIMARY KEY,
                title TEXT,
                artist TEXT,
                album TEXT,
                duration INTEGER,
                thumbnail_url TEXT,
                stream_url TEXT,
                color_start INTEGER,
                color_end INTEGER,
                liked_at INTEGER
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE downloaded_tracks (
                id TEXT PRIMARY KEY,
                title TEXT,
                artist TEXT,
                album TEXT,
                duration INTEGER,
                local_path TEXT,
                thumbnail_path TEXT,
                file_size INTEGER,
                downloaded_at INTEGER
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE custom_playlists (
                id TEXT PRIMARY KEY,
                title TEXT,
                subtitle TEXT,
                track_count INTEGER,
                cover_url TEXT,
                created_at INTEGER
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE playlist_tracks (
                playlist_id TEXT,
                track_id TEXT,
                title TEXT,
                artist TEXT,
                album TEXT,
                duration INTEGER,
                thumbnail_url TEXT,
                position INTEGER,
                PRIMARY KEY (playlist_id, track_id)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE history_tracks (
                id TEXT PRIMARY KEY,
                title TEXT,
                artist TEXT,
                album TEXT,
                duration INTEGER,
                thumbnail_url TEXT,
                stream_url TEXT,
                played_at INTEGER
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE app_settings (
                key TEXT PRIMARY KEY,
                value TEXT
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Safe migration if needed
    }

    fun refreshAll() {
        _likedTracksFlow.value = getLikedTracks()
        _downloadedTracksFlow.value = getDownloadedTracks()
        _playlistsFlow.value = getPlaylists()
        _recentTracksFlow.value = getRecentTracks()
    }

    // ==========================================
    // 1. LIKED TRACKS
    // ==========================================
    fun isTrackLiked(trackId: String): Boolean {
        readableDatabase.rawQuery("SELECT 1 FROM liked_tracks WHERE id = ?", arrayOf(trackId)).use { cursor ->
            return cursor.moveToFirst()
        }
    }

    fun toggleLike(track: Track): Boolean {
        val liked = isTrackLiked(track.id)
        writableDatabase.let { db ->
            if (liked) {
                db.delete("liked_tracks", "id = ?", arrayOf(track.id))
            } else {
                val cv = ContentValues().apply {
                    put("id", track.id)
                    put("title", track.title)
                    put("artist", track.artist)
                    put("album", track.album)
                    put("duration", track.durationSeconds)
                    put("thumbnail_url", track.thumbnailUrl)
                    put("stream_url", track.audioStreamUrl)
                    put("color_start", track.colorStart.toArgb())
                    put("color_end", track.colorEnd.toArgb())
                    put("liked_at", System.currentTimeMillis())
                }
                db.insertWithOnConflict("liked_tracks", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
            }
        }
        _likedTracksFlow.value = getLikedTracks()
        return !liked
    }

    fun getLikedTracks(): List<Track> {
        val list = mutableListOf<Track>()
        readableDatabase.rawQuery(
            "SELECT id, title, artist, album, duration, thumbnail_url, stream_url, color_start, color_end FROM liked_tracks ORDER BY liked_at DESC",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getString(0)
                val title = cursor.getString(1)
                val artist = cursor.getString(2)
                val album = cursor.getString(3) ?: ""
                val duration = cursor.getInt(4)
                val thumbnail = cursor.getString(5) ?: ""
                val streamUrl = cursor.getString(6) ?: ""
                val colStart = cursor.getInt(7)
                val colEnd = cursor.getInt(8)

                list.add(
                    Track(
                        id = id,
                        title = title,
                        artist = artist,
                        album = album,
                        durationSeconds = duration,
                        thumbnailUrl = thumbnail,
                        audioStreamUrl = streamUrl,
                        colorStart = Color(colStart),
                        colorEnd = Color(colEnd)
                    )
                )
            }
        }
        return list
    }

    // ==========================================
    // 2. RECENT / HISTORY TRACKS & VIBE SEED
    // ==========================================
    fun recordTrackPlayed(track: Track) {
        writableDatabase.let { db ->
            val cv = ContentValues().apply {
                put("id", track.id)
                put("title", track.title)
                put("artist", track.artist)
                put("album", track.album)
                put("duration", track.durationSeconds)
                put("thumbnail_url", track.thumbnailUrl)
                put("stream_url", track.audioStreamUrl)
                put("played_at", System.currentTimeMillis())
            }
            db.insertWithOnConflict("history_tracks", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
            setSetting("recentSongId", track.id)
            setSetting("recentSongTitle", track.title)
            setSetting("recentSongArtist", track.artist)
        }
        _recentTracksFlow.value = getRecentTracks()
    }

    fun getRecentTracks(): List<Track> {
        val list = mutableListOf<Track>()
        readableDatabase.rawQuery(
            "SELECT id, title, artist, album, duration, thumbnail_url, stream_url FROM history_tracks ORDER BY played_at DESC LIMIT 30",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(
                    Track(
                        id = cursor.getString(0),
                        title = cursor.getString(1),
                        artist = cursor.getString(2),
                        album = cursor.getString(3) ?: "",
                        durationSeconds = cursor.getInt(4),
                        thumbnailUrl = cursor.getString(5) ?: "",
                        audioStreamUrl = cursor.getString(6) ?: ""
                    )
                )
            }
        }
        return list
    }

    // ==========================================
    // 3. DOWNLOADED TRACKS
    // ==========================================
    fun saveDownloadedTrack(track: Track, localPath: String, thumbPath: String, fileSize: Long) {
        writableDatabase.let { db ->
            val cv = ContentValues().apply {
                put("id", track.id)
                put("title", track.title)
                put("artist", track.artist)
                put("album", track.album)
                put("duration", track.durationSeconds)
                put("local_path", localPath)
                put("thumbnail_path", thumbPath)
                put("file_size", fileSize)
                put("downloaded_at", System.currentTimeMillis())
            }
            db.insertWithOnConflict("downloaded_tracks", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
        }
        _downloadedTracksFlow.value = getDownloadedTracks()
    }

    fun deleteDownloadedTrack(trackId: String) {
        writableDatabase.delete("downloaded_tracks", "id = ?", arrayOf(trackId))
        _downloadedTracksFlow.value = getDownloadedTracks()
    }

    fun isTrackDownloaded(trackId: String): Boolean {
        readableDatabase.rawQuery("SELECT 1 FROM downloaded_tracks WHERE id = ?", arrayOf(trackId)).use { cursor ->
            return cursor.moveToFirst()
        }
    }

    fun getDownloadedTracks(): List<Track> {
        val list = mutableListOf<Track>()
        readableDatabase.rawQuery(
            "SELECT id, title, artist, album, duration, local_path, thumbnail_path FROM downloaded_tracks ORDER BY downloaded_at DESC",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getString(0)
                val title = cursor.getString(1)
                val artist = cursor.getString(2)
                val album = cursor.getString(3) ?: ""
                val duration = cursor.getInt(4)
                val localPath = cursor.getString(5)
                val thumbPath = cursor.getString(6)

                list.add(
                    Track(
                        id = id,
                        title = title,
                        artist = artist,
                        album = album,
                        durationSeconds = duration,
                        thumbnailUrl = "file://$thumbPath",
                        audioStreamUrl = "file://$localPath"
                    )
                )
            }
        }
        return list
    }

    // ==========================================
    // 4. PLAYLISTS
    // ==========================================
    fun createPlaylist(title: String, subtitle: String = "Custom Playlist"): Playlist {
        val id = "pl_" + System.currentTimeMillis()
        writableDatabase.let { db ->
            val cv = ContentValues().apply {
                put("id", id)
                put("title", title)
                put("subtitle", subtitle)
                put("track_count", 0)
                put("cover_url", "")
                put("created_at", System.currentTimeMillis())
            }
            db.insert("custom_playlists", null, cv)
        }
        _playlistsFlow.value = getPlaylists()
        return Playlist(id, title, subtitle, 0)
    }

    fun getPlaylists(): List<Playlist> {
        val list = mutableListOf<Playlist>()
        readableDatabase.rawQuery("SELECT id, title, subtitle, track_count, cover_url FROM custom_playlists ORDER BY created_at DESC", null).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(
                    Playlist(
                        id = cursor.getString(0),
                        title = cursor.getString(1),
                        subtitle = cursor.getString(2) ?: "",
                        trackCount = cursor.getInt(3),
                        coverUrl = cursor.getString(4) ?: ""
                    )
                )
            }
        }
        return list
    }

    // ==========================================
    // 5. APP SETTINGS
    // ==========================================
    fun getSetting(key: String, default: String = ""): String {
        readableDatabase.rawQuery("SELECT value FROM app_settings WHERE key = ?", arrayOf(key)).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getString(0) ?: default else default
        }
    }

    fun setSetting(key: String, value: String) {
        val cv = ContentValues().apply {
            put("key", key)
            put("value", value)
        }
        writableDatabase.insertWithOnConflict("app_settings", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    companion object {
        private const val DATABASE_NAME = "symphony2_music.db"
        private const val DATABASE_VERSION = 1

        @Volatile
        private var instance: SymphonyDatabase? = null

        fun getInstance(context: Context): SymphonyDatabase {
            return instance ?: synchronized(this) {
                instance ?: SymphonyDatabase(context.applicationContext).also { instance = it }
            }
        }
    }
}

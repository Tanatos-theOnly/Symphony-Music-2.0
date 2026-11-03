package com.tanatos.symphonymusic2.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.graphics.Color
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.tanatos.symphonymusic2.data.SymphonyDatabase
import com.tanatos.symphonymusic2.model.LyricLine
import com.tanatos.symphonymusic2.model.SyncedLyrics
import com.tanatos.symphonymusic2.model.SymphonyCatalog
import com.tanatos.symphonymusic2.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SymphonyPlaybackManager private constructor(private val appContext: Context) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    val database = SymphonyDatabase.getInstance(appContext)
    val downloader = SymphonyDownloader.getInstance(appContext)
    val updater = SymphonyUpdater.getInstance(appContext)

    private var exoPlayer: ExoPlayer? = null
    private var tickerJob: Job? = null

    // StateFlows
    private val _currentTrack = MutableStateFlow<Track>(SymphonyCatalog.sampleTracks.first())
    val currentTrack: StateFlow<Track> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackPositionMs = MutableStateFlow(0L)
    val playbackPositionMs: StateFlow<Long> = _playbackPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(SymphonyCatalog.sampleTracks.first().durationSeconds * 1000L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _queue = MutableStateFlow<List<Track>>(SymphonyCatalog.sampleTracks)
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(0) // 0: off, 1: all, 2: one
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _likedIds = MutableStateFlow<Set<String>>(emptySet())
    val likedIds: StateFlow<Set<String>> = _likedIds.asStateFlow()

    private val _activeColors = MutableStateFlow(Pair(Color(0xFFEF4444), Color(0xFF7C3AED)))
    val activeColors: StateFlow<Pair<Color, Color>> = _activeColors.asStateFlow()

    private val _syncedLyrics = MutableStateFlow<SyncedLyrics?>(
        SyncedLyrics(SymphonyCatalog.sampleTracks.first().id, SymphonyCatalog.sampleTracks.first().lyrics)
    )
    val syncedLyrics: StateFlow<SyncedLyrics?> = _syncedLyrics.asStateFlow()

    private val _currentLyricIndex = MutableStateFlow(0)
    val currentLyricIndex: StateFlow<Int> = _currentLyricIndex.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // "Tuned to your vibe" (BOLI - Based On Last Interaction)
    private val _vibeTracks = MutableStateFlow<List<Track>>(emptyList())
    val vibeTracks: StateFlow<List<Track>> = _vibeTracks.asStateFlow()

    val likedTracks = database.likedTracksFlow
    val downloadedTracks = database.downloadedTracksFlow
    val customPlaylists = database.playlistsFlow

    init {
        initPlayer()
        initDatabaseSync()
    }

    private fun initDatabaseSync() {
        // Load persistent likes
        val savedLikes = database.getLikedTracks()
        if (savedLikes.isEmpty()) {
            // Seed initial favorites
            for (t in SymphonyCatalog.sampleTracks.take(2)) {
                database.toggleLike(t)
            }
        }
        _likedIds.value = database.getLikedTracks().map { it.id }.toSet()

        // Check for updates quietly
        updater.checkForUpdates(silent = true)

        // Seed "Tuned to your vibe" based on last interaction
        val lastSongId = database.getSetting("recentSongId")
        if (lastSongId.isNotBlank()) {
            scope.launch(Dispatchers.IO) {
                val vibe = SymphonyMusicEngine.getVibeTracksFor(lastSongId)
                if (vibe.isNotEmpty()) {
                    _vibeTracks.value = vibe
                }
            }
        }
    }

    private fun initPlayer() {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        exoPlayer = ExoPlayer.Builder(appContext)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build().apply {
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(playing: Boolean) {
                        _isPlaying.value = playing
                        if (playing) {
                            startTicker()
                        } else {
                            stopTicker()
                        }
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_READY -> {
                                _isLoading.value = false
                                val dur = duration
                                if (dur > 0) {
                                    _durationMs.value = dur
                                }
                            }
                            Player.STATE_BUFFERING -> {
                                _isLoading.value = true
                            }
                            Player.STATE_ENDED -> {
                                onTrackEnded()
                            }
                            Player.STATE_IDLE -> {
                                _isLoading.value = false
                            }
                        }
                    }
                })
            }
    }

    fun playTrack(track: Track, newQueue: List<Track>? = null) {
        if (newQueue != null && newQueue.isNotEmpty()) {
            _queue.value = newQueue
        }
        _currentTrack.value = track
        _syncedLyrics.value = if (track.lyrics.isNotEmpty()) SyncedLyrics(track.id, track.lyrics) else null
        _currentLyricIndex.value = 0
        _playbackPositionMs.value = 0L
        _durationMs.value = (track.durationSeconds * 1000L).coerceAtLeast(1000L)
        _activeColors.value = Pair(track.colorStart, track.colorEnd)

        // Record in persistent history
        database.recordTrackPlayed(track)

        // Asynchronously update "Tuned to your vibe"
        scope.launch(Dispatchers.IO) {
            val vibe = SymphonyMusicEngine.getVibeTracksFor(track.id)
            if (vibe.isNotEmpty()) {
                _vibeTracks.value = vibe
            }
        }

        // Asynchronously extract dynamic Material You palette & fetch synced lyrics
        extractDynamicPalette(track.thumbnailUrl)
        fetchLyricsForTrack(track)

        scope.launch {
            _isLoading.value = true
            var streamUrl = track.audioStreamUrl

            // If audioStreamUrl is empty or placeholder, resolve from YouTube / Invidious
            if (streamUrl.isBlank() || streamUrl.startsWith("http://placeholder")) {
                val resolved = SymphonyMusicEngine.resolveStreamUrl(track.id)
                if (!resolved.isNullOrBlank()) {
                    streamUrl = resolved
                }
            }

            // Fallback to sample stream if resolution failed
            if (streamUrl.isBlank()) {
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
            }

            withContext(Dispatchers.Main) {
                exoPlayer?.let { player ->
                    val mediaItem = MediaItem.fromUri(streamUrl)
                    player.setMediaItem(mediaItem)
                    player.prepare()
                    player.play()
                }
            }
        }
    }

    fun togglePlayPause() {
        exoPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
            } else {
                if (player.playbackState == Player.STATE_IDLE || player.mediaItemCount == 0) {
                    playTrack(_currentTrack.value)
                } else {
                    player.play()
                }
            }
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
        _playbackPositionMs.value = positionMs
        updateActiveLyricIndex(positionMs)
    }

    fun skipNext() {
        val q = _queue.value
        if (q.isEmpty()) return

        val currentIndex = q.indexOfFirst { it.id == _currentTrack.value.id }
        val nextIndex = if (_isShuffle.value) {
            (q.indices).random()
        } else if (currentIndex in 0 until q.lastIndex) {
            currentIndex + 1
        } else {
            0
        }
        playTrack(q[nextIndex])
    }

    fun skipPrevious() {
        val q = _queue.value
        if (q.isEmpty()) return

        // If played more than 3 seconds, restart current track
        if ((_playbackPositionMs.value) > 3000L) {
            seekTo(0L)
            return
        }

        val currentIndex = q.indexOfFirst { it.id == _currentTrack.value.id }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else q.lastIndex
        playTrack(q[prevIndex])
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun toggleRepeat() {
        _repeatMode.value = (_repeatMode.value + 1) % 3
    }

    fun toggleLike(trackId: String) {
        val track = _queue.value.find { it.id == trackId }
            ?: _vibeTracks.value.find { it.id == trackId }
            ?: _currentTrack.value
        database.toggleLike(track)
        _likedIds.value = database.getLikedTracks().map { it.id }.toSet()
    }

    private fun onTrackEnded() {
        when (_repeatMode.value) {
            2 -> {
                seekTo(0L)
                exoPlayer?.play()
            }
            1 -> skipNext()
            0 -> {
                val q = _queue.value
                val idx = q.indexOfFirst { it.id == _currentTrack.value.id }
                if (idx in 0 until q.lastIndex) {
                    skipNext()
                } else {
                    _isPlaying.value = false
                    seekTo(0L)
                }
            }
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                exoPlayer?.let { player ->
                    val pos = player.currentPosition
                    _playbackPositionMs.value = pos
                    val dur = player.duration
                    if (dur > 0) {
                        _durationMs.value = dur
                    }
                    updateActiveLyricIndex(pos)
                }
                delay(200)
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    private fun updateActiveLyricIndex(positionMs: Long) {
        val lyrics = _syncedLyrics.value ?: return
        val lines = lyrics.lines
        if (lines.isEmpty()) return

        var activeIdx = 0
        for (i in lines.indices) {
            if (positionMs >= lines[i].timestampMs) {
                activeIdx = i
            } else {
                break
            }
        }
        _currentLyricIndex.value = activeIdx
    }

    private fun extractDynamicPalette(thumbnailUrl: String) {
        if (thumbnailUrl.isBlank()) return

        scope.launch(Dispatchers.IO) {
            try {
                val loader = ImageLoader(appContext)
                val request = ImageRequest.Builder(appContext)
                    .data(thumbnailUrl)
                    .allowHardware(false) // Required for Palette extraction
                    .build()

                val result = loader.execute(request)
                if (result is SuccessResult) {
                    val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
                    if (bitmap != null) {
                        Palette.from(bitmap).generate { palette ->
                            if (palette != null) {
                                val dominant = palette.getVibrantColor(
                                    palette.getDominantColor(0xFF4FA5FF.toInt())
                                )
                                val secondary = palette.getDarkVibrantColor(
                                    palette.getMutedColor(0xFF8B5CF6.toInt())
                                )

                                _activeColors.value = Pair(Color(dominant), Color(secondary))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun fetchLyricsForTrack(track: Track) {
        scope.launch(Dispatchers.IO) {
            val lyrics = SymphonyLyricsEngine.fetchLyrics(
                trackId = track.id,
                trackName = track.title,
                artistName = track.artist,
                durationSeconds = track.durationSeconds
            )
            if (lyrics != null && lyrics.lines.isNotEmpty()) {
                _syncedLyrics.value = lyrics
            }
        }
    }

    companion object {
        @Volatile
        private var instance: SymphonyPlaybackManager? = null

        fun getInstance(context: Context): SymphonyPlaybackManager {
            return instance ?: synchronized(this) {
                instance ?: SymphonyPlaybackManager(context.applicationContext).also { instance = it }
            }
        }
    }
}

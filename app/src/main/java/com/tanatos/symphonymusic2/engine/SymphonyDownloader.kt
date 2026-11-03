package com.tanatos.symphonymusic2.engine

import android.content.Context
import com.tanatos.symphonymusic2.data.SymphonyDatabase
import com.tanatos.symphonymusic2.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class SymphonyDownloader private constructor(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val database = SymphonyDatabase.getInstance(context)

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val _downloadProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadProgress: StateFlow<Map<String, Float>> = _downloadProgress.asStateFlow()

    private val downloadDir: File by lazy {
        File(context.filesDir, "downloads").apply {
            if (!exists()) mkdirs()
        }
    }

    fun isDownloaded(trackId: String): Boolean = database.isTrackDownloaded(trackId)

    fun startDownload(track: Track) {
        if (isDownloaded(track.id) || _downloadProgress.value.containsKey(track.id)) return

        scope.launch {
            try {
                updateProgress(track.id, 0.05f)

                // 1. Resolve Stream URL if needed
                var streamUrl = track.audioStreamUrl
                if (streamUrl.isBlank() || streamUrl.startsWith("http://placeholder")) {
                    streamUrl = SymphonyMusicEngine.resolveStreamUrl(track.id).orEmpty()
                }

                if (streamUrl.isBlank()) {
                    updateProgress(track.id, null)
                    return@launch
                }

                updateProgress(track.id, 0.15f)

                // 2. Download Thumbnail
                val thumbFile = File(downloadDir, "${track.id}.jpg")
                if (track.thumbnailUrl.isNotBlank() && !thumbFile.exists()) {
                    try {
                        val thumbReq = Request.Builder().url(track.thumbnailUrl).build()
                        httpClient.newCall(thumbReq).execute().use { res ->
                            if (res.isSuccessful) {
                                res.body?.byteStream()?.use { input ->
                                    FileOutputStream(thumbFile).use { output ->
                                        input.copyTo(output)
                                    }
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }

                updateProgress(track.id, 0.25f)

                // 3. Download Audio Stream
                val audioFile = File(downloadDir, "${track.id}.m4a")
                val audioReq = Request.Builder().url(streamUrl).build()
                httpClient.newCall(audioReq).execute().use { res ->
                    if (!res.isSuccessful) throw Exception("Failed to download stream: HTTP ${res.code}")

                    val body = res.body ?: throw Exception("Empty stream body")
                    val contentLength = body.contentLength()
                    var bytesCopied = 0L

                    body.byteStream().use { input ->
                        FileOutputStream(audioFile).use { output ->
                            val buffer = ByteArray(8 * 1024)
                            var read: Int
                            while (input.read(buffer).also { read = it } != -1) {
                                output.write(buffer, 0, read)
                                bytesCopied += read
                                if (contentLength > 0) {
                                    val pct = 0.25f + 0.75f * (bytesCopied.toFloat() / contentLength.toFloat())
                                    updateProgress(track.id, pct.coerceIn(0.25f, 0.99f))
                                }
                            }
                        }
                    }

                    // 4. Save to Database
                    database.saveDownloadedTrack(
                        track = track,
                        localPath = audioFile.absolutePath,
                        thumbPath = thumbFile.absolutePath,
                        fileSize = audioFile.length()
                    )
                }

                updateProgress(track.id, 1.0f)
                withContext(Dispatchers.IO) {
                    kotlinx.coroutines.delay(800)
                    updateProgress(track.id, null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                updateProgress(track.id, null)
            }
        }
    }

    fun removeDownload(trackId: String) {
        scope.launch {
            try {
                val audioFile = File(downloadDir, "$trackId.m4a")
                val thumbFile = File(downloadDir, "$trackId.jpg")
                if (audioFile.exists()) audioFile.delete()
                if (thumbFile.exists()) thumbFile.delete()
                database.deleteDownloadedTrack(trackId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearAllDownloads() {
        scope.launch {
            try {
                downloadDir.listFiles()?.forEach { it.delete() }
                val downloaded = database.getDownloadedTracks()
                for (t in downloaded) {
                    database.deleteDownloadedTrack(t.id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateProgress(trackId: String, progress: Float?) {
        val current = _downloadProgress.value.toMutableMap()
        if (progress == null) {
            current.remove(trackId)
        } else {
            current[trackId] = progress
        }
        _downloadProgress.value = current
    }

    companion object {
        @Volatile
        private var instance: SymphonyDownloader? = null

        fun getInstance(context: Context): SymphonyDownloader {
            return instance ?: synchronized(this) {
                instance ?: SymphonyDownloader(context.applicationContext).also { instance = it }
            }
        }
    }
}

package com.tanatos.symphonymusic2.engine

import com.tanatos.symphonymusic2.model.LyricLine
import com.tanatos.symphonymusic2.model.SyncedLyrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object SymphonyLyricsEngine {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val lrcLinePattern = Pattern.compile("""^\[(\d{1,2}):(\d{2})(?:\.(\d{1,3}))?\]\s*(.*)$""")

    suspend fun fetchLyrics(
        trackId: String,
        trackName: String,
        artistName: String,
        durationSeconds: Int = 0
    ): SyncedLyrics? = withContext(Dispatchers.IO) {
        try {
            // First try exact match
            val exactUrl = "https://lrclib.net/api/get".toHttpUrlOrNull()?.newBuilder()
                ?.addQueryParameter("track_name", trackName)
                ?.addQueryParameter("artist_name", artistName)
                ?.apply {
                    if (durationSeconds > 0) {
                        addQueryParameter("duration", durationSeconds.toString())
                    }
                }
                ?.build()

            var lrcContent: String? = null

            if (exactUrl != null) {
                val req = Request.Builder()
                    .url(exactUrl)
                    .header("User-Agent", "SymphonyMusic2/2.0.0 (Android; AGSL)")
                    .build()
                client.newCall(req).execute().use { res ->
                    if (res.isSuccessful) {
                        val body = res.body?.string()
                        if (!body.isNullOrBlank()) {
                            val json = JSONObject(body)
                            lrcContent = json.optString("syncedLyrics").takeIf { !it.isNullOrBlank() }
                                ?: json.optString("plainLyrics").takeIf { !it.isNullOrBlank() }
                        }
                    }
                }
            }

            // If not found, try search endpoint
            if (lrcContent == null) {
                val searchUrl = "https://lrclib.net/api/search".toHttpUrlOrNull()?.newBuilder()
                    ?.addQueryParameter("q", "$artistName $trackName")
                    ?.build()

                if (searchUrl != null) {
                    val req = Request.Builder()
                        .url(searchUrl)
                        .header("User-Agent", "SymphonyMusic2/2.0.0 (Android; AGSL)")
                        .build()
                    client.newCall(req).execute().use { res ->
                        if (res.isSuccessful) {
                            val body = res.body?.string()
                            if (!body.isNullOrBlank()) {
                                val arr = JSONArray(body)
                                for (i in 0 until arr.length()) {
                                    val item = arr.getJSONObject(i)
                                    val synced = item.optString("syncedLyrics")
                                    if (synced.isNotBlank()) {
                                        lrcContent = synced
                                        break
                                    }
                                    if (lrcContent == null) {
                                        val plain = item.optString("plainLyrics")
                                        if (plain.isNotBlank()) {
                                            lrcContent = plain
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (lrcContent.isNullOrBlank()) return@withContext null

            val lines = parseLrc(lrcContent)
            if (lines.isNotEmpty()) {
                SyncedLyrics(trackId, lines)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun parseLrc(lrcText: String): List<LyricLine> {
        val result = mutableListOf<LyricLine>()
        val lines = lrcText.lines()

        for (rawLine in lines) {
            val trimmed = rawLine.trim()
            if (trimmed.isEmpty()) continue

            val matcher = lrcLinePattern.matcher(trimmed)
            if (matcher.matches()) {
                val minStr = matcher.group(1) ?: "0"
                val secStr = matcher.group(2) ?: "0"
                val msStr = matcher.group(3) ?: "0"
                val text = matcher.group(4) ?: ""

                val minutes = minStr.toLongOrNull() ?: 0L
                val seconds = secStr.toLongOrNull() ?: 0L
                val ms = when (msStr.length) {
                    1 -> (msStr.toLongOrNull() ?: 0L) * 100
                    2 -> (msStr.toLongOrNull() ?: 0L) * 10
                    3 -> msStr.toLongOrNull() ?: 0L
                    else -> 0L
                }

                val totalMs = (minutes * 60 + seconds) * 1000 + ms
                if (text.isNotBlank() || result.isNotEmpty()) {
                    result.add(LyricLine(totalMs, text))
                }
            } else if (!trimmed.startsWith("[ti:") && !trimmed.startsWith("[ar:") && !trimmed.startsWith("[al:")) {
                // If plain lyrics without timestamps, space them evenly or provide as lines
                if (result.isEmpty()) {
                    result.add(LyricLine(0, trimmed))
                } else {
                    val lastMs = result.last().timestampMs
                    result.add(LyricLine(lastMs + 4000, trimmed))
                }
            }
        }

        return result.sortedBy { it.timestampMs }
    }
}

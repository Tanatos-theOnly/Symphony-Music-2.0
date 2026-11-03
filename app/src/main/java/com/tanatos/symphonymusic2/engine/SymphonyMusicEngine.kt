package com.tanatos.symphonymusic2.engine

import androidx.compose.ui.graphics.Color
import com.tanatos.symphonymusic2.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.random.Random

object SymphonyMusicEngine {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private const val INNERTUBE_API_KEY = "AIzaSyAO_FJ2SlqU8Q4SrYcuHG1HG1HG1HG1" // Standard InnerTube Android VR key
    private const val ANDROID_VR_CLIENT_VERSION = "1.65.10"

    /**
     * Search YouTube Music for tracks matching the query.
     */
    suspend fun searchTracks(query: String): List<Track> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val results = mutableListOf<Track>()

        try {
            val payload = JSONObject().apply {
                put("context", JSONObject().apply {
                    put("client", JSONObject().apply {
                        put("clientName", "WEB_REMIX")
                        put("clientVersion", "1.20240101.01.00")
                        put("hl", "en")
                        put("gl", "US")
                    })
                })
                put("query", query)
                put("params", "Eg-KAQwIABAAGAAgACgAMABqChAEEAMQCRAFEAo%3D") // Filter songs
            }

            val request = Request.Builder()
                .url("https://music.youtube.com/youtubei/v1/search")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Referer", "https://music.youtube.com/")
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string().orEmpty()
                    val json = JSONObject(bodyString)
                    parseSearchResults(json, results)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // If InnerTube search gave empty (or failed due to restriction), fallback to Invidious search
        if (results.isEmpty()) {
            results.addAll(searchViaInvidious(query))
        }

        results
    }

    private fun parseSearchResults(root: JSONObject, outList: MutableList<Track>) {
        try {
            val contents = root.optJSONObject("contents")
                ?.optJSONObject("tabbedSearchResultsRenderer")
                ?.optJSONArray("tabs")
                ?.optJSONObject(0)
                ?.optJSONObject("tabRenderer")
                ?.optJSONObject("content")
                ?.optJSONObject("sectionListRenderer")
                ?.optJSONArray("contents") ?: return

            for (i in 0 until contents.length()) {
                val section = contents.optJSONObject(i)?.optJSONObject("musicShelfRenderer") ?: continue
                val items = section.optJSONArray("contents") ?: continue

                for (j in 0 until items.length()) {
                    val item = items.optJSONObject(j)?.optJSONObject("musicResponsiveListItemRenderer") ?: continue
                    val flexCols = item.optJSONArray("flexColumns") ?: continue

                    // Title
                    val titleObj = flexCols.optJSONObject(0)
                        ?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                        ?.optJSONObject("text")
                    val title = titleObj?.optJSONArray("runs")?.optJSONObject(0)?.optString("text") ?: continue

                    // Artist & album
                    val artistCol = flexCols.optJSONObject(1)
                        ?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                        ?.optJSONObject("text")
                    val artistRuns = artistCol?.optJSONArray("runs")
                    val artist = artistRuns?.optJSONObject(0)?.optString("text") ?: "Artist"
                    val album = if ((artistRuns?.length() ?: 0) > 2) {
                        artistRuns?.optJSONObject(2)?.optString("text") ?: ""
                    } else ""

                    // Video Id
                    val navEndpoint = item.optJSONObject("doubleTapCommand")
                        ?: item.optJSONObject("navigationEndpoint")
                        ?: titleObj.optJSONArray("runs")?.optJSONObject(0)?.optJSONObject("navigationEndpoint")
                    val videoId = navEndpoint?.optJSONObject("watchEndpoint")?.optString("videoId")
                        ?: item.optJSONObject("playlistItemData")?.optString("videoId")
                        ?: ""

                    if (videoId.isNotBlank()) {
                        // Thumbnail
                        val thumbs = item.optJSONObject("thumbnail")
                            ?.optJSONObject("musicThumbnailRenderer")
                            ?.optJSONObject("thumbnail")
                            ?.optJSONArray("thumbnails")
                        val thumbUrl = if (thumbs != null && thumbs.length() > 0) {
                            thumbs.optJSONObject(thumbs.length() - 1)?.optString("url").orEmpty()
                        } else "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"

                        val (startCol, endCol) = generatePaletteColors(title + artist)
                        outList.add(
                            Track(
                                id = videoId,
                                title = title,
                                artist = artist,
                                album = album,
                                durationSeconds = 210,
                                thumbnailUrl = thumbUrl,
                                audioStreamUrl = "",
                                colorStart = startCol,
                                colorEnd = endCol
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun searchViaInvidious(query: String): List<Track> = withContext(Dispatchers.IO) {
        val instances = listOf("https://inv.nadeko.net", "https://invidious.nerdvpn.de")
        for (instance in instances) {
            try {
                val url = "$instance/api/v1/search?q=${java.net.URLEncoder.encode(query, "UTF-8")}&type=video"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0")
                    .build()

                httpClient.newCall(request).execute().use { res ->
                    if (res.isSuccessful) {
                        val body = res.body?.string().orEmpty()
                        val arr = JSONArray(body)
                        val list = mutableListOf<Track>()
                        for (i in 0 until arr.length().coerceAtMost(20)) {
                            val item = arr.getJSONObject(i)
                            val videoId = item.optString("videoId")
                            val title = item.optString("title")
                            val author = item.optString("author")
                            val lengthSec = item.optInt("lengthSeconds", 200)

                            if (videoId.isNotBlank() && title.isNotBlank()) {
                                val (startCol, endCol) = generatePaletteColors(title + author)
                                list.add(
                                    Track(
                                        id = videoId,
                                        title = title,
                                        artist = author,
                                        durationSeconds = lengthSec,
                                        thumbnailUrl = "https://i.ytimg.com/vi/$videoId/hqdefault.jpg",
                                        audioStreamUrl = "",
                                        colorStart = startCol,
                                        colorEnd = endCol
                                    )
                                )
                            }
                        }
                        if (list.isNotEmpty()) return@withContext list
                    }
                }
            } catch (_: Exception) {}
        }
        emptyList()
    }

    /**
     * Resolve playable audio stream URL for a given video ID.
     * Uses YouTube ANDROID_VR client first (unciphered stream), then Invidious fallback.
     */
    suspend fun resolveStreamUrl(videoId: String): String? = withContext(Dispatchers.IO) {
        // Strategy 1: InnerTube player endpoint using ANDROID_VR client
        try {
            val payload = JSONObject().apply {
                put("context", JSONObject().apply {
                    put("client", JSONObject().apply {
                        put("clientName", "ANDROID_VR")
                        put("clientVersion", ANDROID_VR_CLIENT_VERSION)
                        put("deviceMake", "Oculus")
                        put("deviceModel", "Quest 3")
                        put("hl", "en")
                        put("gl", "US")
                    })
                })
                put("videoId", videoId)
                put("playbackContext", JSONObject().apply {
                    put("contentPlaybackContext", JSONObject().apply {
                        put("html5Preference", "HTML5_PREF_WANTS")
                    })
                })
            }

            val request = Request.Builder()
                .url("https://www.youtube.com/youtubei/v1/player")
                .header("User-Agent", "Dalvik/2.1.0 (Linux; U; Android 12; Quest 3 Build/SQ3A.220605.009.A1)")
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string().orEmpty()
                    val json = JSONObject(bodyString)
                    val streamingData = json.optJSONObject("streamingData")
                    val adaptiveFormats = streamingData?.optJSONArray("adaptiveFormats")

                    if (adaptiveFormats != null) {
                        var bestAudioUrl: String? = null
                        var bestBitrate = 0

                        for (i in 0 until adaptiveFormats.length()) {
                            val format = adaptiveFormats.getJSONObject(i)
                            val mimeType = format.optString("mimeType")
                            val url = format.optString("url")
                            val bitrate = format.optInt("bitrate", 0)

                            if (mimeType.startsWith("audio/") && url.isNotBlank()) {
                                if (bitrate > bestBitrate) {
                                    bestBitrate = bitrate
                                    bestAudioUrl = url
                                }
                            }
                        }

                        if (!bestAudioUrl.isNullOrBlank()) {
                            return@withContext bestAudioUrl
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Strategy 2: Invidious audio format endpoint
        val instances = listOf("https://inv.nadeko.net", "https://invidious.nerdvpn.de")
        for (instance in instances) {
            try {
                val url = "$instance/api/v1/videos/$videoId"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0")
                    .build()

                httpClient.newCall(request).execute().use { res ->
                    if (res.isSuccessful) {
                        val body = res.body?.string().orEmpty()
                        val json = JSONObject(body)
                        val adaptive = json.optJSONArray("adaptiveFormats")
                        if (adaptive != null) {
                            for (i in 0 until adaptive.length()) {
                                val fmt = adaptive.getJSONObject(i)
                                val type = fmt.optString("type")
                                val streamUrl = fmt.optString("url")
                                if (type.startsWith("audio/") && streamUrl.isNotBlank()) {
                                    return@withContext streamUrl
                                }
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        null
    }

    private fun generatePaletteColors(seed: String): Pair<Color, Color> {
        val hash = seed.hashCode()
        val hues = listOf(
            Pair(Color(0xFF4FA5FF), Color(0xFF8B5CF6)),
            Pair(Color(0xFFEF4444), Color(0xFFF59E0B)),
            Pair(Color(0xFF10B981), Color(0xFF3B82F6)),
            Pair(Color(0xFFEC4899), Color(0xFF8B5CF6)),
            Pair(Color(0xFF06B6D4), Color(0xFF3B82F6)),
            Pair(Color(0xFFF97316), Color(0xFFEF4444))
        )
        val index = kotlin.math.abs(hash) % hues.size
        return hues[index]
    }
}

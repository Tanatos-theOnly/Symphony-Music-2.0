package com.tanatos.symphonymusic2.engine

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
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
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class SymphonyUpdater private constructor(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    val currentVersionName: String = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "2.0.0"
    } catch (e: Exception) {
        "2.0.0"
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val _isChecking = MutableStateFlow(false)
    val isChecking: StateFlow<Boolean> = _isChecking.asStateFlow()

    private val _updateAvailable = MutableStateFlow(false)
    val updateAvailable: StateFlow<Boolean> = _updateAvailable.asStateFlow()

    private val _latestVersion = MutableStateFlow(currentVersionName)
    val latestVersion: StateFlow<String> = _latestVersion.asStateFlow()

    private val _releaseNotes = MutableStateFlow("")
    val releaseNotes: StateFlow<String> = _releaseNotes.asStateFlow()

    private val _apkDownloadUrl = MutableStateFlow("")
    val apkDownloadUrl: StateFlow<String> = _apkDownloadUrl.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    fun checkForUpdates(silent: Boolean = false) {
        if (_isChecking.value) return
        _isChecking.value = true

        scope.launch {
            try {
                val req = Request.Builder()
                    .url("https://api.github.com/repos/Tanatos-theOnly/Symphony-Music-2.0/releases/latest")
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "Symphony-Updater")
                    .build()

                httpClient.newCall(req).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string().orEmpty()
                        val json = JSONObject(body)
                        val tagName = json.optString("tag_name", "").removePrefix("v")
                        val bodyText = json.optString("body", "")

                        var downloadUrl = ""
                        val assets = json.optJSONArray("assets")
                        if (assets != null) {
                            for (i in 0 until assets.length()) {
                                val asset = assets.getJSONObject(i)
                                val name = asset.optString("name")
                                if (name.endsWith(".apk")) {
                                    downloadUrl = asset.optString("browser_download_url")
                                    break
                                }
                            }
                        }

                        if (isVersionGreater(tagName, currentVersionName)) {
                            _updateAvailable.value = true
                            _latestVersion.value = tagName
                            _releaseNotes.value = bodyText
                            _apkDownloadUrl.value = downloadUrl
                        } else {
                            _updateAvailable.value = false
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isChecking.value = false
            }
        }
    }

    fun downloadAndInstallUpdate() {
        val url = _apkDownloadUrl.value
        if (url.isBlank() || _isDownloading.value) return

        _isDownloading.value = true
        _downloadProgress.value = 0.05f

        scope.launch {
            try {
                val updateDir = File(context.filesDir, "updates").apply { if (!exists()) mkdirs() }
                val apkFile = File(updateDir, "symphony-2.0-update.apk")

                val req = Request.Builder().url(url).build()
                httpClient.newCall(req).execute().use { res ->
                    if (!res.isSuccessful) throw Exception("HTTP ${res.code}")
                    val body = res.body ?: throw Exception("Empty APK body")
                    val contentLength = body.contentLength()
                    var bytesCopied = 0L

                    body.byteStream().use { input ->
                        FileOutputStream(apkFile).use { output ->
                            val buffer = ByteArray(8 * 1024)
                            var read: Int
                            while (input.read(buffer).also { read = it } != -1) {
                                output.write(buffer, 0, read)
                                bytesCopied += read
                                if (contentLength > 0) {
                                    _downloadProgress.value = (bytesCopied.toFloat() / contentLength.toFloat()).coerceIn(0f, 1f)
                                }
                            }
                        }
                    }
                }

                _isDownloading.value = false
                _downloadProgress.value = 1f

                withContext(Dispatchers.Main) {
                    installApk(apkFile)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _isDownloading.value = false
                _downloadProgress.value = 0f
            }
        }
    }

    private fun installApk(apkFile: File) {
        try {
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "com.tanatos.symphonymusic2.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(installIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isVersionGreater(v1: String, v2: String): Boolean {
        val parts1 = v1.split(".").mapNotNull { it.toIntOrNull() }
        val parts2 = v2.split(".").mapNotNull { it.toIntOrNull() }

        val length = maxOf(parts1.size, parts2.size)
        for (i in 0 until length) {
            val num1 = parts1.getOrElse(i) { 0 }
            val num2 = parts2.getOrElse(i) { 0 }
            if (num1 > num2) return true
            if (num1 < num2) return false
        }
        return false
    }

    companion object {
        @Volatile
        private var instance: SymphonyUpdater? = null

        fun getInstance(context: Context): SymphonyUpdater {
            return instance ?: synchronized(this) {
                instance ?: SymphonyUpdater(context.applicationContext).also { instance = it }
            }
        }
    }
}

package com.clipboardreminder.domain

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.clipboardreminder.BuildConfig
import com.clipboardreminder.domain.model.UpdateInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

sealed class UpdateState {
    data object Idle : UpdateState()
    data object Checking : UpdateState()
    data class UpdateAvailable(val info: UpdateInfo) : UpdateState()
    data object NoUpdateAvailable : UpdateState()
    data class Downloading(val progress: Float) : UpdateState()
    data class ReadyToInstall(val file: File) : UpdateState()
    data class Error(val message: String) : UpdateState()
}

@Singleton
class UpdateManager @Inject constructor(
    private val httpClient: HttpClient,
    @ApplicationContext private val context: Context
) {
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState

    // Placeholder URL - User should update this
    private val UPDATE_JSON_URL = "https://raw.githubusercontent.com/gabrozil/Alembrar/main/version.json"

    suspend fun checkForUpdates() {
        _updateState.value = UpdateState.Checking
        try {
            val response: UpdateInfo = httpClient.get(UPDATE_JSON_URL).body()
            val currentVersionCode = BuildConfig.VERSION_CODE

            if (response.versionCode > currentVersionCode) {
                _updateState.value = UpdateState.UpdateAvailable(response)
            } else {
                _updateState.value = UpdateState.NoUpdateAvailable
            }
        } catch (e: Exception) {
            _updateState.value = UpdateState.Error("Falha ao buscar atualizações: ${e.message}")
        }
    }

    suspend fun downloadAndInstall(info: UpdateInfo) {
        _updateState.value = UpdateState.Downloading(0f)
        try {
            val file = File(context.externalCacheDir, "update.apk")
            if (file.exists()) file.delete()

            var bytesRead = 0L
            val response = httpClient.get(info.downloadUrl)
            val contentLength = response.headers[HttpHeaders.ContentLength]?.toLong() ?: 0L
            val channel = response.bodyAsChannel()
            
            val fileChannel = file.writeChannel()
            try {
                val buffer = ByteArray(8192)
                while (!channel.isClosedForRead) {
                    val read = channel.readAvailable(buffer)
                    if (read == -1) break
                    if (read > 0) {
                        fileChannel.writeFully(buffer, 0, read)
                        bytesRead += read
                        if (contentLength > 0) {
                            _updateState.value = UpdateState.Downloading(bytesRead.toFloat() / contentLength)
                        }
                    }
                }
            } finally {
                fileChannel.close()
            }

            _updateState.value = UpdateState.ReadyToInstall(file)
            installApk(file)
        } catch (e: Exception) {
            _updateState.value = UpdateState.Error("Falha no download: ${e.message}")
        }
    }

    fun installApk(file: File) {
        val authority = "${context.packageName}.fileprovider"
        val uri: Uri = FileProvider.getUriForFile(context, authority, file)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun resetState() {
        _updateState.value = UpdateState.Idle
    }
}

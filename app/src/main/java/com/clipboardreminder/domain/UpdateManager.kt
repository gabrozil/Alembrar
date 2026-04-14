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
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
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
            Log.d("UpdateManager", "Iniciando verificação em: $UPDATE_JSON_URL")
            val response: UpdateInfo = httpClient.get(UPDATE_JSON_URL).body()
            val currentVersionCode = BuildConfig.VERSION_CODE
            Log.d("UpdateManager", "Versão atual: $currentVersionCode, Versão GitHub: ${response.versionCode}")

            if (response.versionCode > currentVersionCode) {
                _updateState.value = UpdateState.UpdateAvailable(response)
            } else {
                _updateState.value = UpdateState.NoUpdateAvailable
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "O app já está na versão mais recente.", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            val errorMsg = "Falha ao buscar atualizações: ${e.message}"
            Log.e("UpdateManager", errorMsg, e)
            _updateState.value = UpdateState.Error(errorMsg)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            }
        }
    }

    suspend fun downloadAndInstall(info: UpdateInfo) {
        _updateState.value = UpdateState.Downloading(0f)
        try {
            Log.d("UpdateManager", "Iniciando download de: ${info.downloadUrl}")
            val response = httpClient.get(info.downloadUrl)
            
            if (!response.status.isSuccess()) {
                val errorMsg = "Arquivo não encontrado no GitHub (Erro ${response.status.value}). Verifique se o Release foi criado."
                Log.e("UpdateManager", errorMsg)
                _updateState.value = UpdateState.Error(errorMsg)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                }
                return
            }

            val file = File(context.externalCacheDir, "update.apk")
            if (file.exists()) file.delete()

            var bytesRead = 0L
            val contentLength = response.headers[HttpHeaders.ContentLength]?.toLong() ?: 0L
            val channel = response.bodyAsChannel()
            
            Log.d("UpdateManager", "Tamanho do arquivo: $contentLength bytes")

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

            if (file.length() < 1000) { // APKs não costumam ter menos de 1KB, provavelmente é um erro
                throw Exception("Arquivo baixado parece inválido ou corrompido.")
            }

            _updateState.value = UpdateState.ReadyToInstall(file)
            installApk(file)
        } catch (e: Exception) {
            val errorMsg = "Falha no download: ${e.message}"
            Log.e("UpdateManager", errorMsg, e)
            _updateState.value = UpdateState.Error(errorMsg)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            }
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

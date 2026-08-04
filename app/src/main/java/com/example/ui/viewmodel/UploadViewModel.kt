package com.example.ui.viewmodel

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class UploadViewModel(
    private val repository: AppRepository,
    private val currentUserId: Long
) : ViewModel() {

    private val _type = MutableStateFlow("VIDEO") // "VIDEO" or "PHOTO"
    val type: StateFlow<String> = _type.asStateFlow()

    private val _caption = MutableStateFlow("")
    val caption: StateFlow<String> = _caption.asStateFlow()

    private val _hashtags = MutableStateFlow("")
    val hashtags: StateFlow<String> = _hashtags.asStateFlow()

    private val _selectedMediaUrl = MutableStateFlow("")
    val selectedMediaUrl: StateFlow<String> = _selectedMediaUrl.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _uploadSuccess = MutableStateFlow(false)
    val uploadSuccess: StateFlow<Boolean> = _uploadSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun setType(newType: String) {
        if (_type.value != newType) {
            _type.value = newType
            _errorMessage.value = null
        }
    }

    fun setCaption(text: String) {
        _caption.value = text
    }

    fun setHashtags(text: String) {
        _hashtags.value = text
    }

    fun setMediaUrl(url: String) {
        _selectedMediaUrl.value = url
    }

    fun clearSelectedMedia() {
        _selectedMediaUrl.value = ""
        _errorMessage.value = null
    }

    fun handleSelectedMedia(context: Context, uri: Uri) {
        viewModelScope.launch {
            _errorMessage.value = null
            withContext(Dispatchers.IO) {
                try {
                    // 1. Check File Size (Max 100MB)
                    val pfd = try {
                        context.contentResolver.openFileDescriptor(uri, "r")
                    } catch (e: Exception) {
                        null
                    }
                    val fileSize = pfd?.statSize ?: 0L
                    pfd?.close()

                    if (fileSize > 100 * 1024 * 1024) { // 100 MB limit
                        _errorMessage.value = "حجم الملف كبير جداً (الحد الأقصى 100 ميجابايت)"
                        return@withContext
                    }

                    // 2. If VIDEO, check video duration (Max 60 sec)
                    if (_type.value == "VIDEO") {
                        val retriever = MediaMetadataRetriever()
                        try {
                            retriever.setDataSource(context, uri)
                            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                            val durationMs = durationStr?.toLongOrNull() ?: 0L
                            if (durationMs > 60_000) { // 60 seconds
                                _errorMessage.value = "مدة الفيديو تتجاوز 60 ثانية (الحد الأقصى 60 ثانية)"
                                return@withContext
                            }
                        } catch (_: Exception) {
                            // Ignore metadata parsing error if unreadable
                        } finally {
                            try { retriever.release() } catch (_: Exception) {}
                        }
                    }

                    // 3. Copy file to local private app directory for persistence
                    val mediaDir = File(context.filesDir, "user_media").apply { if (!exists()) mkdirs() }
                    val extension = if (_type.value == "VIDEO") "mp4" else "jpg"
                    val destFile = File(mediaDir, "media_${System.currentTimeMillis()}.$extension")

                    context.contentResolver.openInputStream(uri)?.use { input ->
                        destFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    val persistentUri = Uri.fromFile(destFile).toString()
                    _selectedMediaUrl.value = persistentUri
                    _errorMessage.value = null
                } catch (e: Exception) {
                    _errorMessage.value = "فشل في حفظ الوسيط المحدد: ${e.localizedMessage ?: e.message}"
                }
            }
        }
    }

    fun uploadPost() {
        if (_selectedMediaUrl.value.isBlank()) {
            _errorMessage.value = "يرجى تحديد صورة أو فيديو أولاً"
            return
        }

        if (_caption.value.isBlank()) {
            _errorMessage.value = "يرجى كتابة وصف للمنشور"
            return
        }

        viewModelScope.launch {
            _isUploading.value = true
            _errorMessage.value = null
            try {
                repository.createPost(
                    userId = currentUserId,
                    type = _type.value,
                    mediaUrl = _selectedMediaUrl.value,
                    thumbnailUrl = "",
                    caption = _caption.value,
                    hashtags = _hashtags.value
                )
                _uploadSuccess.value = true
                resetForm()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "حدث خطأ أثناء نشر المحتوى"
            }
            _isUploading.value = false
        }
    }

    fun resetSuccessFlag() {
        _uploadSuccess.value = false
    }

    private fun resetForm() {
        _caption.value = ""
        _hashtags.value = ""
        _selectedMediaUrl.value = ""
    }
}

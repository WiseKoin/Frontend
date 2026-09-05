package com.wisekoin.viewmodel

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisekoin.data.model.TransactionRequest
import com.wisekoin.data.model.VoiceResponseDto
import com.wisekoin.data.remote.RetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class AndroidAudioRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null

    fun start(outputFile: File): Boolean {
        return try {
            outputFile.parentFile?.mkdirs()
            if (outputFile.exists()) {
                outputFile.delete()
            }
            val newRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            newRecorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(96000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            recorder = newRecorder
            true
        } catch (e: Exception) {
            Log.e("AndroidAudioRecorder", "Failed to start recording", e)
            release()
            false
        }
    }

    fun stop(): Boolean {
        return try {
            recorder?.stop()
            recorder?.release()
            recorder = null
            true
        } catch (e: Exception) {
            Log.e("AndroidAudioRecorder", "Failed to stop recording cleanly", e)
            release()
            false
        }
    }

    fun release() {
        try {
            recorder?.release()
        } catch (_: Exception) {}
        recorder = null
    }
}

data class VoiceUiState(
    val isRecording: Boolean = false,
    val recordingSeconds: Int = 0,
    val isProcessing: Boolean = false,
    val voiceResult: VoiceResponseDto? = null,
    val editableAmount: String = "",
    val editableType: String = "EXPENSE",
    val editableCategory: String = "Dining",
    val editableNote: String = "",
    val transcript: String = "",
    val errorMessage: String? = null,
    val isSaveSuccess: Boolean = false
)

class VoiceViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceUiState())
    val uiState: StateFlow<VoiceUiState> = _uiState.asStateFlow()

    private var audioRecorder: AndroidAudioRecorder? = null
    private var currentAudioFile: File? = null
    private var timerJob: Job? = null

    fun startRecording(context: Context) {
        audioRecorder?.release()
        audioRecorder = AndroidAudioRecorder(context.applicationContext)

        val tempFile = File(context.cacheDir, "voice_record_${System.currentTimeMillis()}.m4a")
        currentAudioFile = tempFile

        val started = audioRecorder?.start(tempFile) == true
        if (started) {
            _uiState.update {
                it.copy(
                    isRecording = true,
                    recordingSeconds = 0,
                    errorMessage = null,
                    voiceResult = null
                )
            }
            startTimer()
        } else {
            _uiState.update {
                it.copy(
                    isRecording = false,
                    errorMessage = "Failed to start microphone recording"
                )
            }
        }
    }

    fun stopAndProcess() {
        timerJob?.cancel()

        viewModelScope.launch {
            if (_uiState.value.recordingSeconds < 1) {
                delay(600)
            }

            val stopSuccess = audioRecorder?.stop() == true
            val file = currentAudioFile

            if (!stopSuccess || file == null || !file.exists() || file.length() < 1000L) {
                _uiState.update {
                    it.copy(
                        isRecording = false,
                        isProcessing = false,
                        errorMessage = "Bản ghi âm quá ngắn hoặc chưa thu được âm thanh. Vui lòng nói to rõ ràng ít nhất 1-2 giây."
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    isRecording = false,
                    isProcessing = true,
                    errorMessage = null
                )
            }

            try {
                val requestBody = file.asRequestBody("audio/mp4".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", file.name, requestBody)

                val response = RetrofitClient.receiptApi.extractVoice(part)

                val formattedAmount = try {
                    NumberFormat.getNumberInstance(Locale.US).format(response.amount.toLong())
                } catch (_: Exception) {
                    response.amount.toPlainString()
                }

                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        voiceResult = response,
                        editableAmount = formattedAmount,
                        editableType = if (response.type.equals("INCOME", ignoreCase = true)) "INCOME" else "EXPENSE",
                        editableCategory = if (response.category.isNotBlank() && response.category != "Unknown") response.category else "Dining",
                        editableNote = response.note ?: "",
                        transcript = response.transcript ?: ""
                    )
                }
            } catch (e: Exception) {
                Log.e("VoiceViewModel", "Error extracting voice details", e)
                val detailedError = if (e is retrofit2.HttpException) {
                    try {
                        val body = e.response()?.errorBody()?.string()
                        if (!body.isNullOrBlank()) body else e.localizedMessage
                    } catch (_: Exception) {
                        e.localizedMessage
                    }
                } else {
                    e.localizedMessage
                }
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = detailedError ?: "Cannot extract transaction from voice."
                    )
                }
            }
        }
    }

    fun updateAmount(amount: String) {
        _uiState.update { it.copy(editableAmount = amount) }
    }

    fun updateType(type: String) {
        _uiState.update { it.copy(editableType = type) }
    }

    fun updateCategory(category: String) {
        _uiState.update { it.copy(editableCategory = category) }
    }

    fun updateNote(note: String) {
        _uiState.update { it.copy(editableNote = note) }
    }

    fun confirmAndSave(
        categoryViewModel: CategoryViewModel,
        selectedWalletId: Long?,
        isLoggedIn: Boolean,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentState = _uiState.value

        if (!isLoggedIn) {
            onFailure("Please log in to save transaction.")
            return
        }
        if (selectedWalletId == null) {
            onFailure("Please select a wallet.")
            return
        }

        val parsedAmount = currentState.editableAmount.replace(",", "").replace(".", "").trim().toBigDecimalOrNull()
        if (parsedAmount == null || parsedAmount <= BigDecimal.ZERO) {
            onFailure("Please enter a valid amount.")
            return
        }

        val categories = categoryViewModel.uiState.value.categories
        val selectedCategory = categories.firstOrNull { it.name.equals(currentState.editableCategory, ignoreCase = true) }
            ?: categories.firstOrNull()

        if (selectedCategory == null) {
            onFailure("Category not found. Please create or select a category.")
            return
        }

        viewModelScope.launch {
            try {
                RetrofitClient.transactionApi.createTransaction(
                    TransactionRequest(
                        walletId = selectedWalletId,
                        categoryId = selectedCategory.id,
                        amount = parsedAmount,
                        type = currentState.editableType.uppercase(),
                        note = currentState.editableNote.ifBlank { null },
                        transactionDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    )
                )
                resetState()
                onSuccess()
            } catch (e: Exception) {
                val errorMsg = if (e.message?.contains("Insufficient", ignoreCase = true) == true) {
                    "Insufficient wallet balance."
                } else {
                    e.localizedMessage ?: "Failed to save transaction."
                }
                onFailure(errorMsg)
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { it.copy(recordingSeconds = it.recordingSeconds + 1) }
            }
        }
    }

    fun resetState() {
        timerJob?.cancel()
        audioRecorder?.release()
        try {
            currentAudioFile?.delete()
        } catch (_: Exception) {}
        currentAudioFile = null
        _uiState.value = VoiceUiState()
    }

    override fun onCleared() {
        super.onCleared()
        audioRecorder?.release()
    }
}

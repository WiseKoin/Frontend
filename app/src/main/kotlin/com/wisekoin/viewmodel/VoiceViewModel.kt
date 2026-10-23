package com.wisekoin.viewmodel

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisekoin.data.model.AiCategoryCandidateDto
import com.wisekoin.data.model.AiDuplicateCandidateDto
import com.wisekoin.data.model.AiFieldAmbiguityDto
import com.wisekoin.data.model.TransactionRequest
import com.wisekoin.data.model.VoiceResponseDto
import com.wisekoin.data.model.VoiceTransactionDto
import com.wisekoin.data.remote.NetworkErrorMapper
import com.wisekoin.data.repository.ReceiptRepository
import com.wisekoin.data.repository.TransactionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
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
            Log.e("AndroidAudioRecorder", "Failed to start recording")
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
            Log.e("AndroidAudioRecorder", "Failed to stop recording cleanly")
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
    val transactions: List<VoiceTransactionDto> = emptyList(),
    val currentTransactionIndex: Int = 0,
    val editableAmount: String = "",
    val currency: String = "VND",
    val editableType: String = "",
    val editableCategoryId: Long? = null,
    val editableCategory: String = "",
    val editableNote: String = "",
    val transcript: String = "",
    val occurredAt: String? = null,
    val sourceWalletHint: String = "",
    val destinationWalletHint: String = "",
    val categoryCandidates: List<AiCategoryCandidateDto> = emptyList(),
    val requiresClarification: Boolean = false,
    val ambiguities: List<AiFieldAmbiguityDto> = emptyList(),
    val duplicateCandidates: List<AiDuplicateCandidateDto> = emptyList(),
    val extractionId: String? = null,
    val suggestedSourceWalletId: Long? = null,
    val suggestedDestinationWalletId: Long? = null,
    val ruleApplied: Boolean = false,
    val rememberAiCategory: Boolean = false,
    val errorMessage: String? = null,
    val isSaveSuccess: Boolean = false,
    val isSubmitting: Boolean = false
)

class VoiceViewModel(
    private val receiptRepository: ReceiptRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    companion object {
        const val MAX_RECORDING_SECONDS = 55
    }

    private val _uiState = MutableStateFlow(VoiceUiState())
    val uiState: StateFlow<VoiceUiState> = _uiState.asStateFlow()

    private var audioRecorder: AndroidAudioRecorder? = null
    private var currentAudioFile: File? = null
    private var timerJob: Job? = null
    private val submissionGate = SubmissionGate()

    fun startRecording(context: Context) {
        audioRecorder?.release()
        deleteCurrentAudioFile()
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
            deleteCurrentAudioFile()
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
                deleteCurrentAudioFile()
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

                val response = receiptRepository.extractVoice(part)
                val transactions = response.transactions.orEmpty().ifEmpty {
                    listOf(
                        VoiceTransactionDto(
                            amount = response.amount,
                            currency = response.currency,
                            type = response.type,
                            categoryId = response.categoryId,
                            category = response.category,
                            note = response.note,
                            occurredAt = response.occurredAt,
                            sourceWalletHint = response.sourceWalletHint,
                            destinationWalletHint = response.destinationWalletHint,
                            extractionId = response.extractionId,
                            counterparty = response.counterparty,
                            sourceWalletId = response.sourceWalletId,
                            destinationWalletId = response.destinationWalletId,
                            requiresClarification = response.requiresClarification
                        )
                    )
                }

                _uiState.update {
                    applyTransaction(
                        state = it.copy(
                        isProcessing = false,
                        voiceResult = response,
                            transactions = transactions,
                            transcript = response.transcript ?: ""
                        ),
                        transaction = transactions.first(),
                        index = 0
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = NetworkErrorMapper.message(
                            e,
                            "We couldn’t understand this recording. Try recording again."
                        )
                    )
                }
            } finally {
                deleteCurrentAudioFile()
            }
        }
    }

    fun updateAmount(amount: String) {
        _uiState.update { it.copy(editableAmount = amount) }
    }

    fun updateType(type: String) {
        _uiState.update {
            it.copy(
                editableType = type,
                editableCategoryId = null,
                editableCategory = ""
            )
        }
    }

    fun updateCategory(categoryId: Long, category: String) {
        _uiState.update { it.copy(editableCategoryId = categoryId, editableCategory = category) }
    }

    fun updateNote(note: String) {
        _uiState.update { it.copy(editableNote = note) }
    }

    fun updateRememberAiCategory(remember: Boolean) {
        _uiState.update { it.copy(rememberAiCategory = remember) }
    }

    fun confirmAndSave(
        categoryViewModel: CategoryViewModel,
        selectedWalletId: Long?,
        selectedDestinationWalletId: Long?,
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

        if (currentState.editableType !in setOf("EXPENSE", "INCOME", "TRANSFER")) {
            onFailure("Please choose whether this transaction is an expense, income, or transfer.")
            return
        }

        val parsedAmount = currentState.editableAmount.replace(",", "").trim().toBigDecimalOrNull()
        if (parsedAmount == null || parsedAmount <= BigDecimal.ZERO) {
            onFailure("Please enter a valid amount.")
            return
        }

        val selectedCategory = if (currentState.editableType == "TRANSFER") {
            null
        } else {
            categoryViewModel.uiState.value.categories.firstOrNull {
                it.id == currentState.editableCategoryId &&
                    it.type.equals(currentState.editableType, ignoreCase = true)
            }
        }

        if (currentState.editableType != "TRANSFER" && selectedCategory == null) {
            onFailure("Category not found. Please create or select a category.")
            return
        }
        if (currentState.editableType == "TRANSFER" && selectedDestinationWalletId == null) {
            onFailure("Please select a destination wallet for this transfer.")
            return
        }
        if (currentState.editableType == "TRANSFER" && selectedDestinationWalletId == selectedWalletId) {
            onFailure("Source and destination wallets must be different.")
            return
        }

        if (!submissionGate.tryStart()) return
        _uiState.update { it.copy(isSubmitting = true) }

        viewModelScope.launch {
            try {
                transactionRepository.createTransaction(
                    TransactionRequest(
                        walletId = selectedWalletId,
                        categoryId = selectedCategory?.id,
                        destinationWalletId = selectedDestinationWalletId
                            .takeIf { currentState.editableType == "TRANSFER" },
                        amount = parsedAmount,
                        type = currentState.editableType.uppercase(),
                        note = currentState.editableNote.ifBlank { null },
                        transactionDate = currentState.occurredAt
                            ?: LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        aiExtractionId = currentState.extractionId,
                        rememberAiCategory = currentState.rememberAiCategory
                    )
                )
                val nextIndex = currentState.currentTransactionIndex + 1
                if (nextIndex < currentState.transactions.size) {
                    _uiState.update {
                        applyTransaction(
                            state = it.copy(isSubmitting = false),
                            transaction = currentState.transactions[nextIndex],
                            index = nextIndex
                        )
                    }
                } else {
                    resetState()
                    onSuccess()
                }
            } catch (error: CancellationException) {
                throw error
            } catch (e: Exception) {
                val errorMsg = NetworkErrorMapper.message(e, "Unable to save this transaction. Please try again.")
                onFailure(errorMsg)
            } finally {
                _uiState.update { it.copy(isSubmitting = false) }
                submissionGate.finish()
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.recordingSeconds < MAX_RECORDING_SECONDS) {
                delay(1000)
                _uiState.update { it.copy(recordingSeconds = it.recordingSeconds + 1) }
            }
            if (_uiState.value.isRecording) {
                stopAndProcess()
            }
        }
    }

    private fun applyTransaction(
        state: VoiceUiState,
        transaction: VoiceTransactionDto,
        index: Int
    ): VoiceUiState {
        val formattedAmount = try {
            NumberFormat.getNumberInstance(Locale.US).format(transaction.amount)
        } catch (_: Exception) {
            transaction.amount.toPlainString()
        }
        val extractedType = transaction.type.uppercase()
            .takeIf { it == "EXPENSE" || it == "INCOME" || it == "TRANSFER" }
            .orEmpty()
        return state.copy(
            currentTransactionIndex = index,
            editableAmount = formattedAmount,
            currency = transaction.currency,
            editableType = extractedType,
            editableCategoryId = transaction.categoryId,
            editableCategory = transaction.category
                .takeUnless { it.equals("Unknown", ignoreCase = true) }
                .orEmpty(),
            editableNote = transaction.note ?: "",
            occurredAt = transaction.occurredAt,
            sourceWalletHint = transaction.sourceWalletHint.orEmpty(),
            destinationWalletHint = transaction.destinationWalletHint.orEmpty(),
            categoryCandidates = transaction.categoryCandidates.orEmpty(),
            requiresClarification = transaction.requiresClarification,
            ambiguities = transaction.ambiguities.orEmpty(),
            duplicateCandidates = transaction.duplicateCandidates.orEmpty(),
            extractionId = transaction.extractionId,
            suggestedSourceWalletId = transaction.sourceWalletId,
            suggestedDestinationWalletId = transaction.destinationWalletId,
            ruleApplied = transaction.ruleApplied,
            rememberAiCategory = false
        )
    }

    fun resetState() {
        timerJob?.cancel()
        audioRecorder?.release()
        deleteCurrentAudioFile()
        _uiState.value = VoiceUiState()
    }

    private fun deleteCurrentAudioFile() {
        try {
            currentAudioFile?.delete()
        } catch (_: Exception) {}
        currentAudioFile = null
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        audioRecorder?.release()
        deleteCurrentAudioFile()
    }
}

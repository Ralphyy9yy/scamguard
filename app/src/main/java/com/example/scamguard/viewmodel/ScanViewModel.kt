package com.example.scamguard.viewmodel

import android.content.ContentResolver
import android.provider.Telephony
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamguard.data.model.ScanRecord
import com.example.scamguard.data.model.ScanResultUi
import com.example.scamguard.data.model.SmsMessage
import com.example.scamguard.data.repository.FirebaseAuthRepository
import com.example.scamguard.data.repository.FirestoreScanRepository
import com.example.scamguard.util.ScamDetectorUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScanViewModel(
    private val scanRepository: FirestoreScanRepository,
    private val authRepository: FirebaseAuthRepository
) : ViewModel() {

    private val _smsMessages = MutableLiveData<List<SmsMessage>>(emptyList())
    val smsMessages: LiveData<List<SmsMessage>> = _smsMessages

    private val _isScanning = MutableLiveData(false)
    val isScanning: LiveData<Boolean> = _isScanning

    private val _scanResult = MutableLiveData<ScanResultUi?>()
    val scanResult: LiveData<ScanResultUi?> = _scanResult

    private val _statusMessage = MutableLiveData<String?>()
    val statusMessage: LiveData<String?> = _statusMessage

    fun loadDeviceSms(contentResolver: ContentResolver) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inboxUri = Telephony.Sms.Inbox.CONTENT_URI
                val projection = arrayOf(
                    Telephony.Sms._ID,
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE
                )
                contentResolver.query(
                    inboxUri,
                    projection,
                    null,
                    null,
                    "${Telephony.Sms.DATE} DESC"
                )?.use { cursor ->
                    val messages = mutableListOf<SmsMessage>()
                    val idIndex = cursor.getColumnIndexOrThrow(Telephony.Sms._ID)
                    val addressIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                    val bodyIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
                    val dateIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
                    while (cursor.moveToNext() && messages.size < 50) {
                        messages.add(
                            SmsMessage(
                                id = cursor.getLong(idIndex).toString(),
                                address = cursor.getString(addressIndex),
                                body = cursor.getString(bodyIndex) ?: "",
                                timestamp = cursor.getLong(dateIndex)
                            )
                        )
                    }
                    _smsMessages.postValue(messages)
                }
            } catch (security: SecurityException) {
                _statusMessage.postValue("Permission required to read SMS")
            }
        }
    }

    fun runQuickScan(customMessage: String? = null) {
        val sanitizedCustom = customMessage?.trim()?.takeIf { it.isNotEmpty() }
        val payload = sanitizedCustom ?: QUICK_SCENARIOS.random().content
        performScan(type = "quick", content = payload, simulateDelay = sanitizedCustom == null)
    }

    fun scanSms(message: SmsMessage) {
        performScan(type = "sms", content = message.body)
    }

    fun scanLink(link: String) {
        performScan(type = "qr", content = link)
    }

    fun scanQrPayload(payload: String) {
        performScan(type = "qr", content = payload)
    }

    private fun performScan(
        type: String,
        content: String,
        simulateDelay: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                _isScanning.value = true
                if (simulateDelay) {
                    delay(1800)
                }
                val analysis = ScamDetectorUtil.detect(content)
                val userId = authRepository.ensureUser().uid
                val record = ScanRecord(
                    userId = userId,
                    type = type,
                    content = content,
                    result = analysis.verdict.firestoreValue,
                    timestamp = System.currentTimeMillis()
                )
                withContext(Dispatchers.IO) {
                    scanRepository.saveScan(record)
                }
                _scanResult.value = ScanResultUi(
                    type = type,
                    content = content,
                    verdict = analysis.verdict,
                    reason = analysis.reason,
                    timestamp = record.timestamp
                )
            } catch (ex: Exception) {
                _statusMessage.value = ex.message ?: "Scan failed"
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun clearResult() {
        _scanResult.value = null
    }
}

private data class QuickScanScenario(
    val title: String,
    val content: String
)

private val QUICK_SCENARIOS = listOf(
    QuickScanScenario(
        title = "Bank Verification Push",
        content = "URGENT: Your bank wallet will be locked in 15 minutes. Verify your identity here https://bit.ly/secure-wallet"
    ),
    QuickScanScenario(
        title = "Delivery Reschedule Scam",
        content = "Package on hold. Pay ₱120 redelivery fee at https://courier-support.top to avoid return."
    ),
    QuickScanScenario(
        title = "Investment Pitch",
        content = "Earn 15% daily profit! Transfer $200 now and double tomorrow. Reply YES to join."
    ),
    QuickScanScenario(
        title = "Legit Newsletter",
        content = "ScamGuard digest: This week's tips on staying safe online. No action needed."
    )
)


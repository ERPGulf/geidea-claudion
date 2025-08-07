package com.example.geidea_claudion.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geidea_claudion.data.PaymentRepository
import com.example.geidea_claudion.data.models.PaymentRequest
import com.example.geidea_claudion.data.models.TransactionType
import com.example.geidea_claudion.data.models.PaymentUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PaymentViewModel : ViewModel() {
    private val repository = PaymentRepository()

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    private val _launchIntent = MutableSharedFlow<Intent>()
    val launchIntent: SharedFlow<Intent> = _launchIntent.asSharedFlow()

    fun updateAmount(amount: String) {
        _uiState.update {
            it.copy(
                amount = amount
            )
        }
    }

    fun updateOrderId(orderId: String) {
        _uiState.update { it.copy(orderId = orderId) }
    }

    fun processPurchase(context: Context) {
        processTransaction(context, TransactionType.PURCHASE)
    }

    fun processRefund(context: Context) {
        processTransaction(context, TransactionType.REFUND)
    }

    fun processReversal(context: Context) {
        processTransaction(context, TransactionType.REVERSAL)
    }

    fun handleTransactionResult(data: Intent?) {
        _uiState.update {
            it.copy(isLoading = false)
        }
        val result = repository.parseResponse(data)
        _uiState.update {
            it.copy(
                transactionResult = result
            )
        }
    }

    fun clearResult() {
        _uiState.update {
            it.copy(transactionResult = null)
        }
    }

    fun clearError() {
        _uiState.update {
            it.copy(
                errorMessage = null
            )
        }
    }

    private fun processTransaction(context: Context, type: TransactionType) {
        val currState = _uiState.value
        if (!repository.isAppInstalled((context))) {
            _uiState.update {
                it.copy(
                    errorMessage = "MADA app not installed"
                )
            }
            return
        }

        val amount = currState.amount.toDoubleOrNull() ?: return

        _uiState.update {
            it.copy(isLoading = true, errorMessage = null)
        }

        try {
            val request = PaymentRequest(
                amount = amount,
                orderId = currState.orderId.takeIf {
                    it.isNotBlank()
                }
            )
            val intent = repository.createTransactionIntent(type = type, request = request)
            viewModelScope.launch {
                _launchIntent.emit(intent)
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Unknown Error!"
                )
            }
        }
    }
}
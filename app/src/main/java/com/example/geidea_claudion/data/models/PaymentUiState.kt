package com.example.geidea_claudion.data.models

data class PaymentUiState(
    val amount: String = "",
    val orderId: String = "",
    val isLoading: Boolean = false,
    val transactionResult: TransactionResult? = null,
    val errorMessage: String? = null
) {
    val isAmountValid: Boolean
        get() = amount.toDoubleOrNull()?.let { it > 0 } ?: false

    val canSubmit: Boolean
        get() = isAmountValid && !isLoading
}
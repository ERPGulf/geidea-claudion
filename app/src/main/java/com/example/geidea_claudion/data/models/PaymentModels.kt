package com.example.geidea_claudion.data.models

data class PaymentRequest(val amount: Double, val orderId: String? = null)

data class TransactionResult(
    val status: String,
    val rrn: String? = null,
    val pan: String? = null,
    val authCode: String? = null,
    val amount: String? = null,
    val respCode: String? = null,
    val reason: String? = null
)

enum class TransactionType {
    PURCHASE, REFUND, REVERSAL
}
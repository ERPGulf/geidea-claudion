package com.example.geidea_claudion.data.models

data class PaymentRequest(val amount: Double, val orderId: String? = null,val type: TransactionType)

data class TransactionResult(
    val status: String,
    val rrn: String? = null,
    val pan: String? = null,
    val authCode: String? = null,
    val amount: String? = null,
    val respCode: String? = null,
    val reason: String? = null,
    val orderId: String? = null
)

enum class TransactionType {
    PURCHASE, REFUND, REVERSAL
}

data class MqttPaymentPayload(
    val uuid: String,
    val invoice_number: String,
    val amount: Double,
    val currency: String,
    val customer: String,
    val id: String
)

data class TransactionResultRequest(
    val uuid: String,
    val status: String,
    val amount: Double? = null,
    val invoice_number: String? = null,
    val customer: String? = null,
    val transaction_id: String? = null
)
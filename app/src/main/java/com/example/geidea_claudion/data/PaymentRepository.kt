package com.example.geidea_claudion.data

import android.content.Context
import android.content.Intent
import com.example.geidea_claudion.data.models.PaymentRequest
import com.example.geidea_claudion.data.models.TransactionResult
import com.example.geidea_claudion.data.models.TransactionType
import com.example.geidea_claudion.utils.MadaIntegration

class PaymentRepository {
    private val madaIntegration = MadaIntegration()

    fun isAppInstalled(context: Context): Boolean {
        return madaIntegration.isAppInstalled(context)
    }

    fun createTransactionIntent(type: TransactionType, request: PaymentRequest): Intent {
        return when (type) {
            TransactionType.PURCHASE ->
                madaIntegration.createPurchaseIntent(request = request)

            TransactionType.REFUND ->
                madaIntegration.createReversalIntent(request = request)

            TransactionType.REVERSAL ->
                madaIntegration.createRefundIntent(request = request)
        }
    }

    fun parseResponse(data: Intent?): TransactionResult {
        return madaIntegration.parseTransactionResponse(data = data)
    }
}
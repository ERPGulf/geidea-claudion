package com.example.geidea_claudion.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.example.geidea_claudion.data.models.PaymentRequest
import com.example.geidea_claudion.data.models.TransactionResult
import org.json.JSONObject

class MadaIntegration {
    companion object {
        private const val PAX_APP_PACKAGE_NAME = "com.pax.edc"
    }

    fun isAppInstalled(context: Context): Boolean {
        val pm = context.packageManager
        return try {
            pm.getPackageInfo(PAX_APP_PACKAGE_NAME, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun createPurchaseIntent(request: PaymentRequest): Intent {
        return Intent().apply {
            action = "com.pax.edc.PURCHASE"
            putExtra(Intent.EXTRA_TEXT, request.amount.toString())
            request.orderId?.let { putExtra("ORDER_ID", it) }
            putExtra("CUSTOMER_RECEIPT_FLAG", true)
            putExtra("HOME_BUTTON_STATUS", true)
            type = "text/plain"
        }
    }

    fun createRefundIntent(request: PaymentRequest): Intent {
        return Intent().apply {
            action = "com.pax.edc.REFUND"
            putExtra(Intent.EXTRA_TEXT, request.amount.toString())
            request.orderId?.let { putExtra("ORDER_ID", it) }
            putExtra("CUSTOMER_RECEIPT_FLAG", true)
            type = "text/plain"
        }
    }

    fun createReversalIntent(request: PaymentRequest): Intent {
        return Intent().apply {
            action = "com.pax.edc.REVERSAL"
            request.orderId?.let { putExtra("ORDER_ID", it) }
            type = "text/plain"
        }
    }

    fun parseTransactionResponse(data: Intent?): TransactionResult {
        val status = data?.getStringExtra("status") ?: "Unknown"

        return if (status == "Approved" || status == "Declined") {
            val result = data?.getStringExtra("result")
            result?.let { jsonString ->
                try {
                    val jsonObject = JSONObject(jsonString)
                    TransactionResult(
                        status = status,
                        rrn = jsonObject.optString("rnn"),
                        pan = jsonObject.optString("pan"),
                        authCode = jsonObject.optString("auth_code"),
                        amount = jsonObject.optString("amount"),
                        respCode = jsonObject.optString("resp_code"),
                        orderId = jsonObject.optString("ORDER_ID")
                    )
                } catch (e: Exception) {
                    TransactionResult(status = "Error", reason = "Failed to parse response")
                }
            } ?: TransactionResult(status = "Error", reason = "No result data")
        } else {
            TransactionResult(
                status = status,
                reason = data?.getStringExtra("reason")
            )
        }
    }
}
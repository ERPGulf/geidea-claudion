package com.example.geidea_claudion.services

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.geidea_claudion.data.models.TransactionResultRequest
import com.example.geidea_claudion.utils.ApiClient
import com.example.geidea_claudion.utils.MadaIntegration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransactionProxyActivity : Activity() {
    private val mada = MadaIntegration()
    private val scope = CoroutineScope(Dispatchers.IO)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intentData = intent.getParcelableExtra<Intent>("MADA_INTENT", Intent::class.java)
        if (intentData != null) {
            startActivityForResult(intentData, 1001)
        } else {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            val result = mada.parseTransactionResponse(data)
            val apiResult = TransactionResultRequest(
                uuid = result.orderId ?: "",
                status = result.status,
                amount = result.amount?.toDoubleOrNull()
            )
            // ✅ send result to backend REST API
            // ✅ Launch coroutine to send API request
            scope.launch {
                try {
                    val response = ApiClient.sendPaymentResult(apiResult)
                    Log.d("TransactionProxy", "API Response: $response")
                } catch (e: Exception) {
                    Log.e("TransactionProxy", "Failed to send result", e)
                }
            }
            // Optionally publish to MQTT (if needed later)
            // mqttManager.publish("payments/results", gson.toJson(result))
        }
        finish()
    }

}

package com.example.geidea_claudion.services

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.example.geidea_claudion.utils.MadaIntegration

class TransactionProxyActivity: Activity() {
    private val mada = MadaIntegration()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent.getParcelableExtra<Intent>("MADA_INTENT")
        if (intent != null) {
            startActivityForResult(intent, 1001)
        } else {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            val result = mada.parseTransactionResponse(data)

            // send result to backend REST API
            sendResultToBackend(result)

            // Optionally publish to MQTT
            // mqttManager.publish("payments/results", gson.toJson(result))
        }
        finish()
    }

    private fun sendResultToBackend(result: Any) {
        // make Retrofit/OkHttp POST call to your backend
    }
}
// services/PaymentService.kt
package com.example.geidea_claudion.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.geidea_claudion.data.models.TransactionType
import com.example.geidea_claudion.utils.JsonParser
import com.example.geidea_claudion.utils.MadaIntegration
import com.example.geidea_claudion.utils.MqttManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PaymentService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private lateinit var mqttManager: MqttManager
    private val mada = MadaIntegration()

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        setupMqtt()
    }

    private fun startForegroundService() {
        val channelId = "payment_service_channel"
        val channelName = "Payment Service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Payment Service Running")
            .setContentText("Listening for payment requests...")
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .build()

        startForeground(1, notification)
    }

    private fun setupMqtt() {
        mqttManager = MqttManager(host = "your.mqtt.server.com")

        mqttManager.connect(
            onConnected = {
                mqttManager.subscribe("payments/requests") { message ->
                    handleMqttMessage(message)
                }
            },
            onError = { throwable ->
                throwable.printStackTrace()
            }
        )
    }

    private fun handleMqttMessage(message: String) {
        serviceScope.launch {
            // parse JSON from backend
            val request = JsonParser.parsePaymentRequest(message)
            if (!mada.isAppInstalled(this@PaymentService)) {
                return@launch
            }
            // create intent for Mada app
            val intent = when (request.type) {
                TransactionType.PURCHASE -> mada.createPurchaseIntent(request)
                TransactionType.REFUND -> mada.createRefundIntent(request)
                TransactionType.REVERSAL -> mada.createReversalIntent(request)
            }

            intent.let {
                val proxyIntent =
                    Intent(this@PaymentService, TransactionProxyActivity::class.java).apply {
                        putExtra("MADA_INTENT", it)   // forward Mada intent
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                startActivity(proxyIntent)  // launch proxy activity
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        mqttManager.disconnect()
    }
}

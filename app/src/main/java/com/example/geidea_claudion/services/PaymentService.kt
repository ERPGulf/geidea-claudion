package com.example.geidea_claudion.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.geidea_claudion.data.models.TransactionType
import com.example.geidea_claudion.utils.JsonParser
import com.example.geidea_claudion.utils.MadaIntegration
import com.example.geidea_claudion.utils.MqttManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class




PaymentService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private lateinit var mqttManager: MqttManager
    private val mada = MadaIntegration()

    override fun onCreate() {
        super.onCreate()
        Log.d("PaymentService", "Service created")
        startAsForegroundService()
        setupMqtt()
    }

    private fun startAsForegroundService() {
        val channelId = "payment_service_channel"
        val channelName = "Payment Service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT
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
        mqttManager = MqttManager(
            context = this,
            host = "saudi.claudion.com",
            port = 8883
        )
        mqttManager.connect(
            onConnected = {
                Log.d("PaymentService", "MQTT connected")
                mqttManager.subscribe() { message ->
                    handleMqttMessage(message)
                }
            },
            onError = { throwable ->
                Log.e("PaymentService", "MQTT error", throwable)
            }
        )
    }

    private fun handleMqttMessage(message: String) {
        serviceScope.launch {
            Log.d("PaymentService", "Received MQTT: $message")

            try {
                // Step 1: Parse MQTT payload
                val mqttPayload = JsonParser.parseMqttPayload(message)

                // Step 2: Convert to PaymentRequest (uuid → orderId)
                val request = JsonParser.mqttToPaymentRequest(mqttPayload)

                // Step 3: Ensure Mada app is installed
                if (!mada.isAppInstalled(this@PaymentService)) {
                    Log.w("PaymentService", "Mada app not installed")
                    return@launch
                }

                // Step 4: Build the intent based on transaction type
                val intent = when (request.type) {
                    TransactionType.PURCHASE -> mada.createPurchaseIntent(request)
                    TransactionType.REFUND -> mada.createRefundIntent(request)
                    TransactionType.REVERSAL -> mada.createReversalIntent(request)
                }

                // Step 5: Forward to proxy activity
                val proxyIntent =
                    Intent(this@PaymentService, TransactionProxyActivity::class.java).apply {
                        putExtra("MADA_INTENT", intent)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                startActivity(proxyIntent)
            } catch (e: Exception) {
                Log.e("PaymentService", "Failed to handle MQTT message", e)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d("PaymentService", "Service destroyed, disconnecting MQTT")
        mqttManager.disconnect()
    }
}

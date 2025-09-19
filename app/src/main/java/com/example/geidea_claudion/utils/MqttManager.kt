package com.example.geidea_claudion.utils

import android.content.Context
import android.util.Log
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect
import java.nio.charset.StandardCharsets
import java.util.*

class MqttManager(
    private val context: Context,
    private val host: String,
    private val port: Int = 8883,
    private val clientId: String = "android-client-${UUID.randomUUID()}"
) {

    private lateinit var client: Mqtt3AsyncClient
    private val TAG = "MqttManager"
    private val deviceId = getAndroidId(context)
    // ✅ Hardcoded credentials inside the manager
    private val username = "dK7pX3aN9tB1qL5mR8zF"
    private val password = "L7mQ2tB9pX5aZ3vH8nR1kC6yD4sF0jW"

    fun connect(onConnected: () -> Unit, onError: (Throwable) -> Unit) {
        try {
            Log.d(TAG, "Connecting to MQTT broker at $host:$port with clientId $clientId")

            client = MqttClient.builder()
                .useMqttVersion3()
                .identifier(deviceId)
                .serverHost(host)
                .serverPort(port)
                .sslWithDefaultConfig()
                .buildAsync()

            val connectMessage: Mqtt3Connect = Mqtt3Connect.builder()
                .cleanSession(true)
                .simpleAuth()
                .username(username)
                .password(password.toByteArray(StandardCharsets.UTF_8))
                .applySimpleAuth()
                .build()

            Log.d(TAG, "Attempting to connect to MQTT broker with username $username and password $password")

            client.connect(connectMessage).whenComplete { _, throwable ->
                if (throwable != null) {
                    Log.e(TAG, "MQTT connection failed", throwable)
                    onError(throwable)
                } else {
                    Log.d(TAG, "MQTT connected successfully")
                    onConnected()
                }
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Exception while connecting to MQTT", ex)
            onError(ex)
        }
    }

    fun subscribe(onMessage: (String) -> Unit) {
        val topic = getAndroidId(context)
        Log.d(TAG, "Subscribing to topic $topic")
        if (!::client.isInitialized) {
            Log.e(TAG, "Cannot subscribe: MQTT client not initialized")
            return
        }
        try {
            client.subscribeWith()
                .topicFilter(topic)
                .callback { publish ->
                    val payload = publish.payload.orElse(null)
                    val message = payload?.let { buf ->
                        val copy = ByteArray(buf.remaining())
                        buf.get(copy)  // ✅ safe copy, no ReadOnlyBufferException
                        String(copy, StandardCharsets.UTF_8)
                    }
                    Log.d(TAG, "Received message on topic '$topic': $message")
                    message?.let { onMessage(it) }
                }
                .send()
                .whenComplete { _, throwable ->
                    if (throwable != null) {
                        Log.e(TAG, "Failed to subscribe to topic '$topic'", throwable)
                    } else {
                        Log.d(TAG, "Subscribed to topic '$topic' successfully")
                    }
                }
        } catch (ex: Exception) {
            Log.e(TAG, "Exception during subscription to '$topic'", ex)
        }
    }


    fun publish(topic: String, message: String) {
        if (!::client.isInitialized) {
            Log.e(TAG, "Cannot publish: MQTT client not initialized")
            return
        }
        try {
            client.publishWith()
                .topic(topic)
                .payload(message.toByteArray(StandardCharsets.UTF_8))
                .send()
                .whenComplete { _, throwable ->
                    if (throwable != null) {
                        Log.e(TAG, "Failed to publish message to topic '$topic'", throwable)
                    } else {
                        Log.d(TAG, "Message published to topic '$topic': $message")
                    }
                }
        } catch (ex: Exception) {
            Log.e(TAG, "Exception while publishing to '$topic'", ex)
        }
    }

    fun disconnect() {
        if (::client.isInitialized && client.state.isConnected) {
            client.disconnect().whenComplete { _, throwable ->
                if (throwable != null) {
                    Log.e(TAG, "Error while disconnecting MQTT client", throwable)
                } else {
                    Log.d(TAG, "MQTT client disconnected successfully")
                }
            }
        } else {
            Log.d(TAG, "MQTT client is not connected; no need to disconnect")
        }
    }
}

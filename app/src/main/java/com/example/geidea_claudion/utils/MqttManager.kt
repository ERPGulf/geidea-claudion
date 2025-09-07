package com.example.geidea_claudion.utils

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import java.nio.charset.StandardCharsets
import java.util.UUID

class MqttManager(
    private val host: String,
    private val port: Int = 8883,
    private val clientId: String = "android-client-${UUID.randomUUID()}"
) {
    private lateinit var client: Mqtt3AsyncClient

    fun connect(onConnected: () -> Unit, onError: (Throwable) -> Unit) {
        client = MqttClient.builder()
            .useMqttVersion3()
            .identifier(clientId)
            .serverHost(host)
            .serverPort(port)
            .buildAsync()
        client.connect().whenComplete { _, throwable ->
            if (throwable != null) {
                onError(throwable)
            } else {
                onConnected()
            }
        }
    }

    fun subscribe(topic: String, onMessage: (String) -> Unit) {
        client.subscribeWith()
            .topicFilter(topic)
            .callback { publish ->
                val payload = publish.payload.orElse(null)
                val message = payload?.let { String(it.array(), StandardCharsets.UTF_8) }
                message?.let {
                    onMessage(it)
                }
            }
            .send()
    }

    fun publish(topic: String, message: String) {
        client.publishWith()
            .topic(topic)
            .payload(message.toByteArray(StandardCharsets.UTF_8))
            .send()
    }

    fun disconnect() {
        if (::client.isInitialized && client.state.isConnected) {
            client.disconnect()
        }
    }
}
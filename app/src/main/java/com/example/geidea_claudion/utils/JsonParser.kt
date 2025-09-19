package com.example.geidea_claudion.utils

import com.example.geidea_claudion.data.models.MqttPaymentPayload
import com.example.geidea_claudion.data.models.PaymentRequest
import com.example.geidea_claudion.data.models.TransactionType
import com.google.gson.Gson

object JsonParser {
    private val gson = Gson()
    fun parsePaymentRequest(json: String): PaymentRequest {
        return gson.fromJson(json, PaymentRequest::class.java)
    }

    fun parseMqttPayload(json: String): MqttPaymentPayload {
        return gson.fromJson(json, MqttPaymentPayload::class.java)
    }

    fun mqttToPaymentRequest(mqtt: MqttPaymentPayload): PaymentRequest {
        return PaymentRequest(
            amount = mqtt.amount,
            orderId = mqtt.uuid, // 👈 here you map uuid as orderId
            type = TransactionType.PURCHASE
        )
    }
}
package com.example.geidea_claudion.utils

import com.example.geidea_claudion.data.models.PaymentRequest
import com.google.gson.Gson

object JsonParser {
    private val gson = Gson()
    fun parsePaymentRequest(json: String): PaymentRequest {
        return gson.fromJson(json, PaymentRequest::class.java)
    }
}
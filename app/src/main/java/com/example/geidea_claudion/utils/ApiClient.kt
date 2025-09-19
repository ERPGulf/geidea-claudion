package com.example.geidea_claudion.utils

import com.example.geidea_claudion.data.models.TransactionResultRequest
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object ApiClient {
    private val client = OkHttpClient()
    private val gson = Gson()

    suspend fun sendPaymentResult(payload: TransactionResultRequest): String? = withContext(Dispatchers.IO) {
        val jsonBody = gson.toJson(payload)
        val body = jsonBody.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://myinvois.erpgulf.com:4646/api/method/geidea_erpgulf.geidea_erpgulf.posaw_test.device_callback")
            .addHeader("Content-Type", "application/json")
            .addHeader("Cookie", "full_name=Guest; sid=Guest; system_user=no; user_id=Guest; user_image=")
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Error: ${response.code} - ${response.message}")
            }
            response.body?.string()
        }
    }
}

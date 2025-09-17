package com.example.geidea_claudion

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.geidea_claudion.services.PaymentService
import com.example.geidea_claudion.ui.theme.GeideaclaudionTheme
import com.example.geidea_claudion.ui.view.PaymentScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ✅ Start the PaymentService
        val serviceIntent = Intent(this, PaymentService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)  // Android 8.0+ requires this
        } else {
            startService(serviceIntent)
        }

        // ✅ Your Compose UI
        setContent {
            GeideaclaudionTheme {
                PaymentScreen()
            }
        }
    }
}

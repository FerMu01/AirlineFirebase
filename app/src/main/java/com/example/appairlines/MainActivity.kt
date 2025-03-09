package com.example.appairlines

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Switch
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    private lateinit var switchNotifications: Switch
    private lateinit var sharedPreferences: SharedPreferences

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) Log.d("FCM", "Permiso concedido")
        else Log.w("FCM", "Permiso denegado")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        switchNotifications = findViewById(R.id.switch1)
        sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        checkNotificationPermission()
        createNotificationChannel()
        setupSwitch()
        setupWindowInsets()
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                "canal_importante",
                "Promociones",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para ofertas especiales"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }.let { channel ->
                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        ).createNotificationChannel(channel)
            }
        }
    }

    private fun setupSwitch() {
        val isSubscribed = sharedPreferences.getBoolean("notifications_enabled", true)
        switchNotifications.isChecked = isSubscribed

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                subscribeToNotifications()
            } else {
                unsubscribeFromNotifications()
            }
            sharedPreferences.edit().putBoolean("notifications_enabled", isChecked).apply()
        }

        // Configurar suscripción inicial basada en preferencias
        if (isSubscribed) {
            subscribeToNotifications()
        }
    }

    private fun subscribeToNotifications() {
        FirebaseMessaging.getInstance().subscribeToTopic("promociones")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FCM", "✅ Suscrito a promociones")
                } else {
                    Log.e("FCM", "❌ Error en suscripción", task.exception)
                }
            }
    }

    private fun unsubscribeFromNotifications() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic("promociones")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FCM", "🚫 Desuscrito de promociones")
                } else {
                    Log.e("FCM", "❌ Error en desuscripción", task.exception)
                }
            }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}

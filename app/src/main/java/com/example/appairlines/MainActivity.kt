package com.example.appairlines

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    private lateinit var switchNotifications: Switch
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var rvNotifications: RecyclerView
    private lateinit var notificationAdapter: NotificationAdapter
    private val notificationsList = mutableListOf<NotificationItem>()
    private lateinit var firestore: FirebaseFirestore
    private var notificationsListener: ListenerRegistration? = null

    private val requestPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) Log.d("FCM", "Permiso concedido")
        else Log.w("FCM", "Permiso denegado")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.statusBarColor = android.graphics.Color.parseColor("#66B1F2")


        switchNotifications = findViewById(R.id.switch1)
        sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        checkNotificationPermission()
        createNotificationChannel()
        setupSwitch()
        setupWindowInsets()

        rvNotifications = findViewById(R.id.rvNotifications)
        notificationAdapter = NotificationAdapter(notificationsList)
        rvNotifications.adapter = notificationAdapter
        rvNotifications.layoutManager = LinearLayoutManager(this)

        firestore = FirebaseFirestore.getInstance()
        subscribeToNotificationsFirestore()
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.app.NotificationChannel(
                "canal_importante",
                "Promociones",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para ofertas especiales"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }.let { channel ->
                (getSystemService(android.content.Context.NOTIFICATION_SERVICE)
                        as android.app.NotificationManager).createNotificationChannel(channel)
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

    private fun subscribeToNotificationsFirestore() {
        notificationsListener = firestore.collection("notificaciones")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("Firestore", "Error al escuchar cambios", e)
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    val notifications = snapshots.documents.mapNotNull { doc ->
                        doc.toObject(NotificationItem::class.java)?.let { item ->
                            NotificationItem(
                                titulo = item.titulo,
                                mensaje = doc.getString("promocion") ?: "",
                                timestamp = item.timestamp
                            )
                        }
                    }
                    notificationAdapter.setNotifications(notifications)
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationsListener?.remove()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}

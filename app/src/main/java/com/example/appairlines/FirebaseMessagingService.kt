package com.example.appairlines

import android.app.NotificationManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM_DEBUG", "Mensaje recibido: ${remoteMessage.data}")

        remoteMessage.notification?.let { notification ->
            showNotification(
                notification.title ?: "¡Nueva promoción!",
                notification.body ?: "Oferta especial disponible"
            )
        }
    }

    override fun onNewToken(token: String) {
        Log.d("FCM_TOKEN", "🔑 Nuevo token: $token")
        super.onNewToken(token)
    }

    private fun showNotification(title: String, body: String) {
        NotificationCompat.Builder(this, "canal_importante").apply {
            setSmallIcon(R.drawable.ic_notification)
            setContentTitle(title)
            setContentText(body)
            priority = NotificationCompat.PRIORITY_MAX
            setAutoCancel(true)
            setCategory(NotificationCompat.CATEGORY_PROMO)
        }.build().let { notification ->
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    ).notify(System.currentTimeMillis().toInt(), notification)
        }
    }
}
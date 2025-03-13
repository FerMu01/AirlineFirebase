package com.example.appairlines

import com.google.firebase.Timestamp

data class NotificationItem(
    val titulo: String = "",
    val mensaje: String = "",
    val timestamp: Timestamp? = null
)

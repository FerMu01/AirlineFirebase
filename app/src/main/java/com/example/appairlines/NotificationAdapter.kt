package com.example.appairlines

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(private val notifications: MutableList<NotificationItem>)
    : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitulo: TextView = itemView.findViewById(R.id.tvTitulo)
        val tvMensaje: TextView = itemView.findViewById(R.id.tvMensaje)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.tvTitulo.text = notification.titulo
        holder.tvMensaje.text = notification.mensaje

        notification.timestamp?.toDate()?.let { date ->
            val formatoFecha = SimpleDateFormat("EEEE dd/MM/yyyy HH:mm", Locale("es", "ES"))
            val fechaFormateada = formatoFecha.format(date).replaceFirstChar { it.uppercase() }
            holder.tvTimestamp.text = fechaFormateada
        } ?: run {
            holder.tvTimestamp.text = ""
        }
    }

    override fun getItemCount(): Int = notifications.size

    fun addNotification(notification: NotificationItem) {
        notifications.add(0, notification)
        if (notifications.size > 15) {
            notifications.removeAt(notifications.size - 1)
        }
        notifyDataSetChanged()
    }

    fun setNotifications(newNotifications: List<NotificationItem>) {
        notifications.clear()
        notifications.addAll(newNotifications.take(15))
        notifyDataSetChanged()
    }
}

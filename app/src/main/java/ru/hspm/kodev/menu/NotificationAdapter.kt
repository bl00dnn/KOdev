package ru.hspm.kodev.menu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import ru.hspm.kodev.R
import ru.hspm.kodev.models.Notification
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
    private var notifications: List<Notification>,
    private val onItemClick: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    fun updateNotifications(newNotifications: List<Notification>) {
        this.notifications = newNotifications
        notifyDataSetChanged()
    }

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.notificationTitle)
        private val messageView: TextView = itemView.findViewById(R.id.notificationMessage)
        private val dateView: TextView = itemView.findViewById(R.id.notificationDate)
        private val cardView: MaterialCardView = itemView.findViewById(R.id.notificationCard)

        fun bind(notification: Notification) {
            titleView.text = notification.title
            messageView.text = notification.message
            dateView.text = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                .format(notification.date)

            val context = itemView.context

            cardView.strokeWidth = if (notification.isRead) 0 else 1

            itemView.setOnClickListener {
                onItemClick(notification)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    override fun getItemCount() = notifications.size
}
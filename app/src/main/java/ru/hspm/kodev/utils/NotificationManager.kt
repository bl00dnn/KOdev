package ru.hspm.kodev.utils

import android.content.Context
import ru.hspm.kodev.models.Notification
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

object NotificationManager {
    private const val PREFS_NAME = "notifications_prefs"
    private const val KEY_NOTIFICATIONS = "notifications_list"

    fun addNotification(context: Context, title: String, message: String) {
        val notifications = getNotifications(context).toMutableList()
        notifications.add(0, Notification(
            id = notifications.size + 1,
            title = title,
            message = message,
            date = Date()
        ))
        saveNotifications(context, notifications)
    }

    fun getNotifications(context: Context): List<Notification> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_NOTIFICATIONS, null) ?: return emptyList()
        val type = object : TypeToken<List<Notification>>() {}.type
        return Gson().fromJson(json, type)
    }

    private fun saveNotifications(context: Context, notifications: List<Notification>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_NOTIFICATIONS, Gson().toJson(notifications)).apply()
    }

    fun markAsRead(context: Context, notificationId: Int) {
        val notifications = getNotifications(context).toMutableList()
        notifications.find { it.id == notificationId }?.let {
            notifications[notifications.indexOf(it)] = it.copy(isRead = true)
            saveNotifications(context, notifications)
        }
    }

    fun clearAllNotifications(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_NOTIFICATIONS).apply()
    }
}
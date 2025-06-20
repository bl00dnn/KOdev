// Notification.kt
package ru.hspm.kodev.models

import java.util.*

data class Notification(
    val id: Int,
    val title: String,
    val message: String,
    val date: Date,
    val isRead: Boolean = false
)
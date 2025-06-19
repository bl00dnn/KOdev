package ru.hspm.kodev.models

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Notification(
    val id: String = "",
    val type: String = "", // "password_reset", "profile_update" и т.д.
    val message: String = "",
    @field:ServerTimestamp
    val timestamp: Date? = null,
    val read: Boolean = false
)
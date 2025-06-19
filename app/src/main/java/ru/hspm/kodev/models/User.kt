package ru.hspm.kodev.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class User(
    @DocumentId
    val id: String = "",

    @get:PropertyName("first_name")
    @set:PropertyName("first_name")
    var firstName: String = "",

    @get:PropertyName("last_name")
    @set:PropertyName("last_name")
    var lastName: String = "",

    val email: String = ""
)
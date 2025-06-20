package ru.hspm.kodev.models

data class ArtStationProject(
    val id: Int,
    val title: String,
    val cover_url: String,
    val hash_id: String,
    val published_at: String,
    val likes_count: Int,
    val views_count: Int
)
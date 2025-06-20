package ru.hspm.kodev.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import ru.hspm.kodev.models.ArtStationApiResponse

interface ArtStationApi {
    @GET("users/{username}/projects.json")
    fun getUserProjects(@Path("username") username: String): Call<ArtStationApiResponse>
}
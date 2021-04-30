package me.pedro.yeelight.advisor

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiAdvisor {
    @GET(value = "/api/v1/forecast/locale/{cityId}/hours/72")
    suspend fun getForecast(@Path("cityId") cityId: Int, @Query("token") token: String): Response
}
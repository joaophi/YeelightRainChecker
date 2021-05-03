package me.pedro.yeelight.advisor

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
import com.squareup.moshi.ToJson
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object LocalDateTimeAdapter {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    @FromJson
    fun fromJson(date: String): LocalDateTime = LocalDateTime.parse(date, formatter)

    @ToJson
    fun toJson(date: LocalDateTime): String = date.format(formatter)
}

@JsonClass(generateAdapter = true)
data class Response(
    val data: List<Data>,
)

@JsonClass(generateAdapter = true)
data class Data(
    val date: LocalDateTime,
    val rain: Rain,
)

@JsonClass(generateAdapter = true)
data class Rain(
    val precipitation: Double,
)

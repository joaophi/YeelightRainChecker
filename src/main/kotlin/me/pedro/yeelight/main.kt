package me.pedro.yeelight

import com.squareup.moshi.Moshi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retry
import me.pedro.yeelight.advisor.ApiAdvisor
import me.pedro.yeelight.advisor.LocalDateTimeAdapter
import me.pedro.yeelight.yeelight.*
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

private val API_TOKEN: String = System.getenv("API_TOKEN") ?: throw Exception("API_TOKEN not set")

private val CITY_ID: Int = System.getenv("CITY_ID").toIntOrNull() ?: throw Exception("CITY_ID not set")

private val WAKE_TIME: LocalTime = LocalTime.parse(System.getenv("WAKE_TIME") ?: throw Exception("WAKE_TIME not set"))

private val YEELIGHT_HOST: String = System.getenv("YEELIGHT_HOST") ?: throw Exception("YEELIGHT_HOST not set")

private val API_ADVISOR: ApiAdvisor = run {
    val moshi = Moshi.Builder()
        .add(LocalDateTimeAdapter)
        .build()

    val retrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .baseUrl("https://apiadvisor.climatempo.com.br/")
        .build()

    retrofit.create()
}

suspend fun main(): Unit = generateSequence(LocalDate.now()) { it.plusDays(1) }
    .filterNot { it.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY) }
    .map(WAKE_TIME::atDate)
    .filter(LocalDateTime.now()::isBefore)
    .onEach { println("next: $it") }
    .map { ChronoUnit.MILLIS.between(LocalDateTime.now(), it) }
    .asFlow()
    .onEach(::delay)
    .onEach {
        println("running")
        val willRain = try {
            val start = LocalDate.now().atTime(WAKE_TIME)
            val end = LocalDate.now().atTime(17, 0)
            API_ADVISOR
                .getForecast(CITY_ID, API_TOKEN)
                .data
                .asSequence()
                .filter { it.date in start..end }
                .any { (_, rain) -> rain.precipitation > 0 }
        } catch (e: Throwable) {
            println("error accessing weather api: ${e.message ?: e}")
            null
        }
        println("will rain: $willRain")

        val yeelightDevice = YeelightDevice(YEELIGHT_HOST)
        yeelightDevice.use {
            val color = when (willRain) {
                true -> 0x0000FF
                false -> 0x00FF00
                null -> 0xFF0000
            }

            val command = Command.SetScene(
                Scene.CF(
                    count = 3,
                    onFinish = OnCFFinish.POWER_OFF,
                    CFAction.Color(duration = 5_000, color, brightness = 100),
                    CFAction.Color(duration = 55_000, value = 0xFFFFFF, brightness = 100),
                    CFAction.Sleep(duration = 180_000),
                )
            )
            yeelightDevice.sendCommand(command)
        }
    }
    .retry { println("error: ${it.message ?: it}"); true }
    .collect()
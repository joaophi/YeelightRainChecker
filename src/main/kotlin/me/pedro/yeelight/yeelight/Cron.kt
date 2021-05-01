package me.pedro.yeelight.yeelight

enum class CronType(val value: Int) {
    POWER_OFF(value = 0)
}

data class Cron(
    val type: CronType,
    val delay: Int,
    val mix: Int,
)

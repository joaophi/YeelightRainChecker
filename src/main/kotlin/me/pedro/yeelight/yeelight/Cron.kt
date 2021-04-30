package me.pedro.yeelight.yeelight

data class Cron(
    val type: CronType,
    val delay: Int,
    val mix: Int,
)
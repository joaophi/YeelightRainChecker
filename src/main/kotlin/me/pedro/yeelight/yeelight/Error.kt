package me.pedro.yeelight.yeelight

data class Error(
    val code: Int,
    override val message: String,
) : Throwable(message)
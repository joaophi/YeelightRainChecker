package me.pedro.yeelight.yeelight

fun String.toBoolean(): Boolean = when (this) {
    "on", "1" -> true
    "off", "0" -> false
    else -> throw Exception("cannot parse '$this' to Boolean")
}

fun Boolean.toOnOff(): String = if (this) "on" else "off"

fun String.toLightMode(): LightMode = toInt().toLightMode()

fun Int.toLightMode() = LightMode.values().first { it.id == this }

fun List<CFAction>.toFlowExpression(): String =
    joinToString(separator = ",") { "${it.duration},${it.mode},${it.value},${it.brightness}" }
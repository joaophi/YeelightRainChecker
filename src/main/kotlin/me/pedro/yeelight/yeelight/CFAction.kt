package me.pedro.yeelight.yeelight

sealed class CFAction(open val duration: Int, val mode: Int, open val value: Int, open val brightness: Int) {
    data class Color(override val duration: Int, override val value: Int, override val brightness: Int) :
        CFAction(duration, mode = 1, value, brightness)

    data class Temperature(override val duration: Int, override val value: Int, override val brightness: Int) :
        CFAction(duration, mode = 2, value, brightness)

    data class Sleep(override val duration: Int) : CFAction(duration, mode = 7, value = 0, brightness = 0)
}
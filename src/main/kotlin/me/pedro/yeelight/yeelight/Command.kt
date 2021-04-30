package me.pedro.yeelight.yeelight

import me.pedro.yeelight.yeelight.Command.GetProp.Properties
import java.net.ServerSocket

private fun String.parseString(): String = this

private fun String.parseInt(): Int = toIntOrNull() ?: throw Exception()

private fun String.parseBoolean(): Boolean = when (this) {
    "on", "1" -> true
    "off", "0" -> false
    else -> throw Exception()
}

private fun String.parseLightMode(): LightMode = parseInt().toLightMode()

private fun Int.toLightMode() = LightMode.values().first { it.id == this }

private fun Boolean.toOnOff(): String = if (this) "on" else "off"

sealed class Effect(val value: String, open val duration: Int) {
    object Sudden : Effect(value = "sudden", duration = 0)
    data class Smooth(override val duration: Int) : Effect(value = "smooth", duration)
}

enum class LightMode(val id: Int) { RGB(id = 1), CT(id = 2), HSV(id = 3) }

sealed class Command<R : Any>(val method: String) {
    open val params: List<Any> get() = emptyList()

    @Suppress("UNCHECKED_CAST")
    open fun parseResult(result: List<String>): R = (result.firstOrNull() == "ok") as R

    data class GetProp(val props: List<Property<*>>) : Command<Properties>(method = "get_prop") {
        constructor(vararg properties: Property<*>) : this(properties.toList())

        sealed class Property<T>(val name: String, val parse: (String) -> T) {
            object Power : Property<Boolean>(name = "power", parse = String::parseBoolean)
            object Bright : Property<Int>(name = "bright", parse = String::parseInt)
            object CT : Property<Int>(name = "ct", parse = String::parseInt)
            object RGB : Property<Int>(name = "rgb", parse = String::parseInt)
            object HUE : Property<Int>(name = "hue", parse = String::parseInt)
            object SAT : Property<Int>(name = "sat", parse = String::parseInt)
            object ColorMode : Property<LightMode>(name = "color_mode", parse = String::parseLightMode)
            object Flowing : Property<Boolean>(name = "flowing", parse = String::parseBoolean)
            object DelayOFF : Property<Int>(name = "delayoff", parse = String::parseInt)
            object MusicOn : Property<Boolean>(name = "music_on", parse = String::parseBoolean)
            object Name : Property<String>(name = "name", parse = String::parseString)
            object BackgroundPower : Property<Boolean>(name = "bg_power", parse = String::parseBoolean)
            object BackgroundFlowing : Property<Boolean>(name = "bg_flowing", parse = String::parseBoolean)
            object BackgroundFlowParams : Property<String>(name = "bg_flow_params", parse = String::parseString)
            object BackgroundCT : Property<Int>(name = "bg_ct", parse = String::parseInt)
            object BackgroundLightMode : Property<LightMode>(name = "bg_lmode", parse = String::parseLightMode)
            object BackgroundBight : Property<Int>(name = "bg_bright", parse = String::parseInt)
            object BackgroundRGB : Property<Int>(name = "bg_rgb", parse = String::parseInt)
            object BackgroundHUE : Property<Int>(name = "bg_hue", parse = String::parseInt)
            object BackgroundSAT : Property<Int>(name = "bg_sat", parse = String::parseInt)
            object NightLightBrightness : Property<Int>(name = "nl_br", parse = String::parseInt)
            object ActiveMode : Property<Boolean>(name = "active_mode", parse = String::parseBoolean)
        }

        data class Properties(private val map: Map<String, String>) {
            operator fun <T> get(property: Property<T>): T? = map[property.name]?.let(property.parse)
        }

        override val params: List<Any> get() = props.map(Property<*>::name)

        override fun parseResult(result: List<String>) = Properties(props.map(Property<*>::name).zip(result).toMap())
    }

    data class SetBright(val brightness: Int, val effect: Effect) : Command<Boolean>(method = "set_bright") {
        override val params: List<Any> get() = listOf(brightness, effect.value, effect.duration)
    }

    data class SetMusic(val mode: Mode) : Command<Boolean>(method = "set_music") {
        sealed class Mode(val action: Int) {
            data class On(val host: String, val port: Int) : Mode(action = 1) {
                constructor(server: ServerSocket) : this(server.inetAddress.hostAddress, server.localPort)
            }

            object Off : Mode(action = 0)
        }

        override val params: List<Any>
            get() = when (mode) {
                is Mode.On -> listOf(mode.action, mode.host, mode.port)
                Mode.Off -> listOf(mode.action)
            }
    }

    data class SetPower(val power: Boolean, val effect: Effect) : Command<Boolean>(method = "set_power") {
        override val params: List<Any> get() = listOf(power.toOnOff(), effect.value, effect.duration)
    }

    data class SetScene(val scene: Scene) : Command<Boolean>(method = "set_scene") {
        sealed class Scene(val clazz: String, val val1: Int, val val2: Int, val val3: String? = null) {
            data class Color(val color: Int, val brightness: Int) : Scene(clazz = "color", color, brightness)
            data class HSV(val color: Int, val brightness: Int) : Scene(clazz = "hsv", color, brightness)
            data class CT(val ct: Int, val brightness: Int) : Scene(clazz = "ct", ct, brightness)
            data class CF(
                val count: Int = 0,
                val onFinish: OnFinish = OnFinish.KEEP_STATE,
                val actions: List<Action>,
            ) : Scene(
                clazz = "cf",
                count,
                onFinish.id,
                actions.joinToString(separator = ",") { "${it.duration},${it.mode},${it.value},${it.brightness}" }
            ) {
                constructor(count: Int = 0, onFinish: OnFinish = OnFinish.KEEP_STATE, vararg actions: Action) :
                        this(count, onFinish, actions.toList())

                sealed class Action(
                    open val duration: Int,
                    val mode: Int,
                    open val value: Int,
                    open val brightness: Int,
                ) {
                    data class Color(
                        override val duration: Int,
                        override val value: Int,
                        override val brightness: Int,
                    ) : Action(duration, mode = 1, value, brightness)

                    data class Temperature(
                        override val duration: Int,
                        override val value: Int,
                        override val brightness: Int,
                    ) : Action(duration, mode = 2, value, brightness)

                    data class Sleep(
                        override val duration: Int,
                    ) : Action(duration, mode = 7, value = 0, brightness = 0)
                }

                enum class OnFinish(val id: Int) {
                    REVERT_STATE(id = 0),
                    KEEP_STATE(id = 1),
                    POWER_OFF(id = 2);
                }
            }

            data class AutoDelayOff(val brightness: Int, val timer: Int) :
                Scene(clazz = "auto_delay_off", brightness, timer)
        }

        override val params: List<Any> get() = listOfNotNull(scene.clazz, scene.val1, scene.val2, scene.val3)
    }

    object Toggle : Command<Boolean>(method = "toggle")
}
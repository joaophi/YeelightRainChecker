package me.pedro.yeelight.yeelight

import java.net.ServerSocket

sealed class MusicMode(val action: Int, open val host: String? = null, open val port: Int? = null) {
    data class On(override val host: String, override val port: Int) : MusicMode(action = 1, host, port) {
        constructor(server: ServerSocket) : this(server.inetAddress.hostAddress, server.localPort)
    }

    object Off : MusicMode(action = 0)
}
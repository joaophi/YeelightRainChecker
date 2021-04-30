package me.pedro.yeelight.yeelight

sealed class Response

data class Notification(
    val method: String,
    val params: Map<String, Any>,
) : Response()

sealed class Result(
    open val id: Int,
) : Response()

data class SuccessResult(
    override val id: Int,
    val result: List<Any>,
) : Result(id)

data class ErrorResult(
    override val id: Int,
    val error: Error,
) : Result(id)
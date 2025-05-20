package moe.fuqiuluo.signfaker.http.ext

import io.ktor.http.Parameters
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.Locale
import kotlin.experimental.xor

@Serializable
data class APIResult<T>(
    val code: Int,
    val msg: String = "",
    @Contextual
    val data: T? = null
)

// query string
suspend fun ApplicationCall.queryParam(key: String, def: String? = null, err: String? = null): String? {
    if (request.queryParameters[key].isNullOrBlank() && def == null) {
        val errorMsg = err ?: "Missing parameter '$key'"
        respond(APIResult<Nothing>(1, errorMsg))
        return null
    }
    return request.queryParameters[key] ?: def
}

// form-urlencoded
suspend fun ApplicationCall.formParam(key: String, def: String? = null, err: String? = null): String? {
    val postParams: Parameters = receiveParameters()
    if (postParams[key].isNullOrBlank() && def == null) {
        val errorMsg = err ?: "Missing parameter '$key'"
        respond(APIResult<Nothing>(1, errorMsg))
        return null
    }
    return postParams[key] ?: def
}

suspend fun ApplicationCall.failure(code: Int, msg: String) {
    respond(APIResult(code, msg, "failed"))
}

@JvmOverloads fun String.hex2ByteArray(replace: Boolean = false): ByteArray {
    val s = if (replace) this.replace(" ", "")
        .replace("\n", "")
        .replace("\t", "")
        .replace("\r", "") else this
    val bs = ByteArray(s.length / 2)
    for (i in 0 until s.length / 2) {
        bs[i] = s.substring(i * 2, i * 2 + 2).toInt(16).toByte()
    }
    return bs
}

@JvmOverloads fun ByteArray.toHexString(uppercase: Boolean = true): String = this.joinToString("") {
    (it.toInt() and 0xFF).toString(16)
        .padStart(2, '0')
        .let { s -> if (uppercase) s.lowercase(Locale.getDefault()) else s }
}

fun ByteArray.xor(key: ByteArray): ByteArray {
    val result = ByteArray(this.size)
    for (i in 0 until this.size) {
        result[i] = (this[i] xor key[i % key.size] xor ((i and 0xFF).toByte()))
    }
    return result
}

fun ByteArray.toAsciiHexString() = joinToString("") {
    if (it in 32..127) it.toInt().toChar().toString() else "{${
        it.toUByte().toString(16).padStart(2, '0').uppercase(
            Locale.getDefault())
    }}"
}
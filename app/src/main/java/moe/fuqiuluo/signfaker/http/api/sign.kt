package moe.fuqiuluo.signfaker.http.api

import com.tencent.mobileqq.fe.FEKit
import com.tencent.mobileqq.qsec.qsecurity.QSec
import com.tencent.mobileqq.qsec.qsecurity.QSecConfig
import io.ktor.server.routing.Routing

import com.tencent.mobileqq.sign.QQSecuritySign
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import moe.fuqiuluo.signfaker.http.ext.APIResult
import moe.fuqiuluo.signfaker.http.ext.queryParam
import moe.fuqiuluo.signfaker.http.ext.formParam
import moe.fuqiuluo.signfaker.http.ext.failure
import moe.fuqiuluo.signfaker.http.ext.hex2ByteArray
import moe.fuqiuluo.signfaker.http.ext.toHexString


fun Routing.sign() {
    get("/sign") {
        val uin = call.queryParam("uin")!!
        val qua = call.queryParam("qua", QSecConfig.business_qua)!!
        val cmd = call.queryParam("cmd")!!
        val seq = call.queryParam("seq")?.toIntOrNull() ?: return@get call.failure(1, "Invalid seq")
        val buffer = call.queryParam("buffer")!!.hex2ByteArray()
        val qimei36 = call.queryParam("qimei36", def = QSecConfig.business_q36)!!

        call.requestSign(cmd, uin, qua, seq, buffer, qimei36)
    }

    post("/sign") {
        val param = call.receiveParameters()
        val uin = call.formParam("uin")!!
        val qua = call.formParam("qua", QSecConfig.business_qua)!!
        val cmd = call.formParam("cmd")!!
        val seq = call.formParam("seq")?.toIntOrNull() ?: return@post call.failure(1, "Invalid seq")
        val buffer = call.formParam("buffer")?.hex2ByteArray() ?: return@post call.failure(1, "Invalid buffer")
        val qimei36 = call.formParam("qimei36", def = "")!!

        call.requestSign(cmd, uin, qua, seq, buffer, qimei36)
    }
}

@Serializable
private data class Sign(
    val token: String,
    val extra: String,
    val sign: String,
    val o3did: String
)

private suspend fun ApplicationCall.requestSign(cmd: String, uin: String, qua: String, seq: Int, buffer: ByteArray, qimei36: String = QSecConfig.business_q36) {
    FEKit.changeUin(uin.toLong())

    fun int32ToBuf(i: Int): ByteArray {
        val out = ByteArray(4)
        out[3] = i.toByte()
        out[2] = (i shr 8).toByte()
        out[1] = (i shr 16).toByte()
        out[0] = (i shr 24).toByte()
        return out
    }

    val sign = QQSecuritySign.getSign(QSec, qua, cmd, buffer, int32ToBuf(seq), uin)!!

    respond(
        APIResult(0, "success", Sign(
            sign.token.toHexString(),
            sign.extra.toHexString(),
            sign.sign.toHexString(), QSecConfig.business_o3did ?: ""
        ))
    )
}
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
import io.ktor.util.pipeline.*
import kotlinx.serialization.Serializable
import moe.fuqiuluo.signfaker.http.ext.APIResult
import moe.fuqiuluo.signfaker.http.ext.hex2ByteArray
import moe.fuqiuluo.signfaker.http.ext.toHexString
import io.ktor.server.plugins.BadRequestException


fun Routing.sign() {
    get("/sign") {
        val uin = call.request.queryParameters["uin"] ?: throw BadRequestException("Missing 'uin'")
        val qua = call.request.queryParameters["qua"] ?: QSecConfig.business_qua
        val cmd = call.request.queryParameters["cmd"] ?: throw BadRequestException("Missing 'cmd'")
        val seq = call.request.queryParameters["seq"]?.toInt() ?: throw BadRequestException("Missing or invalid 'seq'")
        val buffer = call.request.queryParameters["buffer"]?.hex2ByteArray() ?: throw BadRequestException("Missing 'buffer'")
        val qimei36 = call.request.queryParameters["qimei36"] ?: QSecConfig.business_q36

        call.requestSign(cmd, uin, qua, seq, buffer, qimei36)
    }

    post("/sign") {
        val param = call.receiveParameters()
        val uin = param["uin"] ?: throw BadRequestException("Missing 'uin'")
        val qua = param["qua"] ?: QSecConfig.business_qua
        val cmd = param["cmd"] ?: throw BadRequestException("Missing 'cmd'")
        val seq = param["seq"]?.toInt() ?: throw BadRequestException("Missing or invalid 'seq'")
        val buffer = param["buffer"]?.hex2ByteArray() ?: throw BadRequestException("Missing 'buffer'")
        val qimei36 = param["qimei36"] ?: ""

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
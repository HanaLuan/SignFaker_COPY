package moe.fuqiuluo.signfaker.http.api

import android.content.Context
import android.content.pm.PackageManager
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import moe.fuqiuluo.signfaker.http.ext.APIResult
import moe.fuqiuluo.signfaker.logger.TextLogger.log
import online.eruru.Config

@Serializable
data class APIInfo(
    val version: String,
    val appVersion: String,
    val protocol: String,
    val pid: UInt
)

fun Routing.index() {
    get("/") {
        val out = APIResult(0, "IAA", APIInfo(
            version = "1.1.6",
            appVersion = "${Config.AppVersionName}/${Config.AppVersionCode}",
            protocol = "v2.1.7",
            pid = android.os.Process.myPid().toUInt()
        ))
        kotlin.runCatching {
            println(out)
            println("[HttpResp] ${Json.encodeToString(out)}")
        }.onFailure {
            it.printStackTrace()
        }
        call.respond(out)
    }
}
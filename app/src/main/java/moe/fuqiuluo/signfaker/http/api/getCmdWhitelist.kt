package moe.fuqiuluo.signfaker.http.api

import com.tencent.mobileqq.channel.ChannelManager.getCmdWhiteList
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import moe.fuqiuluo.signfaker.http.ext.APIResult

fun Routing.getCmdWhitelist() {
    get("/get_cmd_whitelist") {
        call.respond(
            APIResult(0, "success",
                getCmdWhiteList()
            )
        )
    }
}
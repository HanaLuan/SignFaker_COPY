package com.tencent.mobileqq.channel

import com.tencent.mobileqq.fe.CmdWhiteListChangeCallback

object ChannelManager {

    external fun getCmdWhiteList(): ArrayList<String>

    external fun initReport(str: String, str2: String)

    external fun onNativeReceive(str: String, bArr: ByteArray, z: Boolean, j2: Long)

    external fun sendMessageTest()

    external fun setChannelProxy(channelProxy: ChannelProxy?)

    // external fun setCmdWhiteListChangeCallback(cmdWhiteListChangeCallback: CmdWhiteListChangeCallback)
}
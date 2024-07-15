package com.tencent.mobileqq.sign

import com.tencent.mobileqq.fe.EventCallback
import com.tencent.mobileqq.qsec.qsecurity.QSec


object QQSecuritySign {
    val EMPTY_BYTE_ARRAY = ByteArray(0)

    external fun initSafeMode(isSafe: Boolean)

    external fun getSign(
        qSec: QSec,
        str: String,
        str2: String,
        bArr: ByteArray
    ): SignResult?

    external fun dispatchEvent(str: String, str2: String)

    external fun dispatchEventPB(str: String, str2: String, bArr: ByteArray, eventCallback: EventCallback)

    external fun notify(str: String, str2: String, str3: String, eventCallback: EventCallback)

    external fun notifyCamera(str: String?, str2: String?, str3: String?, str4: String?, str5: String?, str6: String?, eventCallback: EventCallback?)

    external fun requestToken()

    private fun int32ToBuf(i10: Int): ByteArray {
        return byteArrayOf(
            (i10 shr 24).toByte(),
            (i10 shr 16).toByte(),
            (i10 shr 8).toByte(), i10.toByte()
        )
    }

    external fun notifyFaceDetect(str: String?, str2: String?, str3: String?, eventCallback: EventCallback?)

    external fun safeUiReport(str: String?, str2: String?, str3: String?, eventCallback: EventCallback?)

    external fun uiNotify(str: String?, str2: String?, str3: String?, eventCallback: EventCallback?)

    class SignResult {
        var extra: ByteArray = EMPTY_BYTE_ARRAY
        var sign: ByteArray = EMPTY_BYTE_ARRAY
        var token: ByteArray = EMPTY_BYTE_ARRAY
    }
}
package com.tencent.mobileqq.qsec.qsecurity

import android.content.Context
import com.tencent.mobileqq.qsec.qsecdandelionsdk.Dandelion
import com.tencent.mobileqq.qsec.qsecprotocol.ByteData


object QSec {
    external fun doReport(str: String, str2: String, str3: String, str4: String): Int

    external fun doSomething(context: Context, i2: Int): Int

    external fun getXwDebugID(str: String?): ByteArray?

    fun updateO3DID(str: String) {
        QSecConfig.business_o3did = str
    }

    fun getSign(str: String?, bArr: ByteArray?): ByteArray? {
        return ByteData.getSign(QSecConfig.business_uin!!, str, bArr)
    }

    fun getSignEntry(str: String?, bArr: ByteArray?): ByteArray? {
        if (QSecConfig.sign_strategy == 0) {
            return getLiteSign(str, bArr)
        }
        return getSign(str, bArr)
    }

    private fun int32_to_buf(bArr: ByteArray, i: Int, i2: Int) {
        bArr[i + 3] = (i2 shr 0).toByte()
        bArr[i + 2] = (i2 shr 8).toByte()
        bArr[i + 1] = (i2 shr 16).toByte()
        bArr[i + 0] = (i2 shr 24).toByte()
    }


    private fun getSignWithTail(bArr: ByteArray): ByteArray {
        val bArr2 = ByteArray(bArr.size + 4)
        System.arraycopy(bArr, 0, bArr2, 0, bArr.size)
        int32_to_buf(bArr2, bArr.size, QSecConfig.sign_tail)
        return bArr2
    }

    private fun getLiteSign(str: String?, bArr: ByteArray?): ByteArray {
        return getSignWithTail(Dandelion.fly(str, bArr)!!)
    }


}
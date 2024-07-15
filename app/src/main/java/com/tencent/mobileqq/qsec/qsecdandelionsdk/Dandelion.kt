package com.tencent.mobileqq.qsec.qsecdandelionsdk

object Dandelion {
    external fun energy(data: Any, salt: Any): ByteArray?

    fun fly(str: String?, bArr: ByteArray?): ByteArray? {
        val bytes = "".toByteArray()
        if (true) {
            try {
                return energy(str!!, bArr!!)
            } catch (th: Throwable) {
                th.printStackTrace()
            }
        }
        return bytes
    }

}
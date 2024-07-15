package com.tencent.mobileqq.qsec.qsecprotocol

import android.annotation.SuppressLint
import android.content.Context
import com.tencent.mobileqq.dt.app.Dtc.ctx
import com.tencent.mobileqq.qsec.qsecurity.QSecConfig



@SuppressLint("StaticFieldLeak")
object ByteData {

    private var mThradName: String = "com.tencent.qqlite:MSF"
    private var status: ByteArray = byteArrayOf(0, 0, 0, 0)
    private var mUin: String = QSecConfig.business_uin.toString()?: "0"
    private var cData: Nothing? = null
    private var handlerThread: Nothing? = null
    private var mContext: Context = ctx.get()!!
    private var mBmpMgr: Nothing? = null
    private var mPoxyNativeLoaded: Boolean = true
    private var mPoxyInit: Boolean = true
    private lateinit var filterName: String
    private var filterStatus: Boolean = true

    private external fun getByte(context: Context, j: Long, j2: Long, j3: Long, j4: Long, obj: Any?, obj2: Any?, obj3: Any?, obj4: Any?): ByteArray?

    private fun checkObject(j: Long, obj: Any?): Boolean {
        if (j == 0L) {
            return false
        }
        return obj !is ByteArray || obj.isEmpty()
    }

    private fun getCode(j: Long, j2: Long, j3: Long, j4: Long, obj: Any?, obj2: Any?, obj3: Any, obj4: Any?, obj5: Any?): ByteArray? {
        if (checkObject(j, obj4)) {
            this.status[3] = 12.toByte()
            return this.status
        }
        if (this.status[1].toInt() != 0 || !this.mPoxyNativeLoaded || !this.mPoxyInit) {
            if (this.status[3].toInt() == 0) {
                this.status[3] = 13.toByte()
            }
            return this.status
        }

        val arrayList = ArrayList<Any?>()
        arrayList.add("armeabi")
        arrayList.add(1.toString())
        arrayList.add(obj3 as String?)
        arrayList.add(obj5 as String?)
        arrayList.add(0x00)
        arrayList.add(0x00)
        arrayList.add(0x00)
        arrayList.add(0x00)
        arrayList.add(this.mThradName)
        val context = ctx.get()!!
        return getByte(context, j, j2, j3, j4, arrayList.toTypedArray() as Array<String?>, obj2, obj3, obj4)
    }

    fun getSign(str: String, str2: String?, bArr: ByteArray?): ByteArray? {
        return getCode(1L, 0L, 0L, 0L, "", "", str, bArr, str2)
    }


}
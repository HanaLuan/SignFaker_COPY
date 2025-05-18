package moe.fuqiuluo.signfaker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.updatePadding
import com.tencent.beacon.event.UserAction
import com.tencent.mmkv.MMKV
import com.tencent.mobileqq.channel.ChannelManager
import com.tencent.mobileqq.channel.ChannelProxy
import com.tencent.mobileqq.dt.app.Dtc
import com.tencent.mobileqq.fe.FEKit
import com.tencent.mobileqq.qsec.qsecurity.QSecConfig
import com.tencent.mobileqq.sign.QQSecuritySign
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.fuqiuluo.signfaker.http.HttpServer
import moe.fuqiuluo.signfaker.logger.TextLogger
import moe.fuqiuluo.signfaker.logger.TextLogger.log
import moe.fuqiuluo.signfaker.proxy.ProxyContext
import online.eruru.Config
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import java.security.Security
import java.util.concurrent.atomic.AtomicBoolean


class MainActivity : AppCompatActivity() {

    var global = moe.fuqiuluo.signfaker.ext.GlobalData()

    init {
        Security.addProvider(BouncyCastleProvider())
        global["PACKET"] = arrayListOf<moe.fuqiuluo.signfaker.ext.SsoPacket>()
    }

    private val isInit = AtomicBoolean(false)

    lateinit var input: EditText
    lateinit var send: Button

    private fun getAppPackageName(packageManager: PackageManager): String {
        try {
            // val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            val appPackageInfo = packageManager.getPackageInfo(packageName, 0)
            Config.AppVersionName = appPackageInfo.versionName
            Config.AppVersionCode = appPackageInfo.versionCode.toString()
            val realPkgName = appPackageInfo.packageName
            log("获取到自身包名: $realPkgName")
            return realPkgName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        log("获取自身包名失败...")
        return "moe.hanahime.signfaker"
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val scrollView = findViewById<View>(R.id.scrollView)
        val bottomBar = findViewById<View>(R.id.bottomBar)
        /* ViewCompat.setFitsSystemWindows(scrollView, true)
        ViewCompat.setFitsSystemWindows(bottomBar, true)
        ViewCompat.setFitsSystemWindows(findViewById(android.R.id.content), true)
        val decorView = window.decorView
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        )
        window.statusBarColor = 0
        window.navigationBarColor = 0 */
        // androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, true)

        // 处理顶部、底部、左右安全区和软键盘
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(scrollView) { view, insets ->
            val sysBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.ime())
            view.updatePadding(
                top = sysBars.top,
                left = sysBars.left,
                right = sysBars.right
            )
            insets
        }
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(bottomBar) { view, insets ->
            val sysBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.ime())
            // 软键盘弹出时优先用ime.bottom，否则用系统栏
            val bottomInset = kotlin.math.max(sysBars.bottom, ime.bottom)
            view.updatePadding(
                bottom = bottomInset,
                left = sysBars.left,
                right = sysBars.right
            )
            insets
        }

        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        val controller = androidx.core.view.WindowInsetsControllerCompat(window, window.decorView)
        val isDarkTheme = when (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) {
            android.content.res.Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }
        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = !isDarkTheme
        controller.isAppearanceLightNavigationBars = false

        val text = findViewById<TextView>(R.id.text)
        TextLogger.updateTextHandler = object: Handler(mainLooper) {
            @SuppressLint("SetTextI18n")
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                if (msg.what == TextLogger.WHAT_INFO) {
                    text.text = text.text.toString() + "\n" + msg.obj
                }
            }
        }

        input = findViewById(R.id.input)
        send = findViewById(R.id.send)

        MMKV.initialize(this)  // 程序入口初始化 MMKV
        val mmkv= MMKV.mmkvWithID("ruru")  // 通过ID拿到对应MMKV进行操作，在任何地方都可以随时使用
        mmkv.putString("Hi", "Hello")
        mmkv.encode("Eruru", "World")//可能是加密存储
        log(String.format("%s %s!",
            mmkv.getString("Hi", "Something Wrongs").toString(),
            mmkv.decodeString("Eruru", "Please Check").toString()))

        // 弹出授权界面
        val REQUEST_CODE = 100
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:$packageName"))
                startActivityForResult(intent, REQUEST_CODE)
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), REQUEST_CODE)
        }

        // 测试一下
        try {
            Config.AppPackageName = getAppPackageName(packageManager);
            val fileName = "Config.json"
            val directory = File (String.format ("%s/%s", Environment.getExternalStorageDirectory (), Config.AppPackageName));
            log(directory.path)
            directory.mkdirs()
            log(directory.exists().toString())
            val file = File (String.format ("%s/%s", directory, fileName));
            log(file.path)
            file.createNewFile();
            FileOutputStream(file).use { fos ->
                fos.write("Hello, World!".toByteArray())
            }
        } catch (e : Exception) {
            log(e.toString ())
        }

        if (isInit.compareAndSet(false, true)) {
            GlobalScope.launch {
                initServer()
                //initCodec()
                initFEKit()
                initCommand()
            }
        }
    }

    private fun initCommand() {
        send.setOnClickListener {
            TextLogger.input(">>> ${input.text}")
            val command = input.text.toString()
            if (command == "refresh_token" || command == "token" || command == "request_token") {
                QQSecuritySign.requestToken()
                log("Call QQSecuritySign.requestToken()")
            }
            input.setText("")
        }
    }

    private suspend fun initServer() {
        log("请输入启动在哪个服务器端口：")

        var port = 0
        send.setOnClickListener {
            TextLogger.input(">>> ${input.text}")
            kotlin.runCatching {
                val myPort = input.text.toString().toInt()
                if (myPort !in 1000 .. 64000) {
                    throw IllegalArgumentException()
                } else {
                    port = myPort
                }
            }.onFailure {
                input.setText("")
                log("错误输入，请重新尝试")
            }
        }

        while (port == 0) {
            delay(3000)
            log("期待输入中 (3s) ...... ")
        }

        log("服务器将运行在端口：$port")

        HttpServer(port)
    }

    @OptIn(ExperimentalStdlibApi::class)
    private suspend fun initFEKit() {
        val ctx = ProxyContext(this)
        Dtc.ctx = WeakReference(ctx)
        UserAction.initUserAction(ctx, false)
        UserAction.setAppKey("0S200MNJT807V3E")
        UserAction.setAppVersion("8.9.68")

        var qimei = ""
        UserAction.getQimei {
            log("QIMEI FETCH 成功： $it")
            qimei = it.toString()
            QSecConfig.business_q36 = it.toString()
        }

        val qua = "V1_AND_SQ_8.9.68_4264_YYB_D"

        val cs = contentResolver
        log("预设androidId为：${Settings.System.getString(cs, "android_id")}")

        log("请输入你的androidId：")
        var androidId = ""
        send.setOnClickListener {
            TextLogger.input(">>> ${input.text}")
            androidId = input.text.toString()
            input.setText("")
        }
        while (androidId.isEmpty()) {
            delay(3000)
            log("期许输入中 (3s) ...... ")
        }
        log("你的androidId输入为[$androidId]")
        Settings.System.putString(cs, "android_id", androidId)
        Dtc.androidId = androidId
        log("尝试载入FEKIT二进制库...")
        System.loadLibrary("fekit")
        log("载入FEKIT二进制库成功...")
        FEKit.init(qua, qimei, androidId, ctx)
/*        ChannelManager.setChannelProxy(object: ChannelProxy() {
            override fun sendMessage(cmd: String, buffer: ByteArray, id: Long) {
                log("ChannelProxy.sendMessage($cmd, $buffer, $id)")
            }
        })*/
        ChannelManager.setChannelProxy(object: ChannelProxy() {
            override fun sendMessage(cmd: String, buffer: ByteArray, id: Long) {
                val hex = buffer.toHexString()
                if (id == -1L) return
                (global["PACKET"] as ArrayList<moe.fuqiuluo.signfaker.ext.SsoPacket>).add(moe.fuqiuluo.signfaker.ext.SsoPacket(cmd, hex, id))
                log("ChannelProxy.sendMessage($cmd, $buffer, $id)")
                return
            }
        })
        ChannelManager.initReport(QSecConfig.business_qua, "7.0.300", Build.VERSION.RELEASE, Build.BRAND + Build.MODEL, QSecConfig.business_q36, QSecConfig.business_guid)
        ChannelManager.setCmdWhiteListChangeCallback {
            it.forEach {
                log("Register for cmd: $it")
            }
        }
    }

    /*
    private fun initCodec() {
        val codec = object: CodecWarpper() {
            override fun onInvalidData(i2: Int, i3: Int, str: String) {
                log("onInvalidData")
            }

            override fun onInvalidSign() {
                log("onInvalidSign")
            }

            override fun onResponse(i2: Int, obj: Any?, i3: Int) {
                log("onResponse1")
            }

            override fun onResponse(i2: Int, obj: Any?, i3: Int, bArr: ByteArray?) {
                log("onResponse2")
            }

            override fun onSSOPingResponse(bArr: ByteArray?, i2: Int): Int {
                return 0
            }
        }
        CodecWarpper.getFileStoreKey()
        val hashSet: HashSet<String> = hashSetOf()
        hashSet.add(BaseConstants.CMD_LOGIN_AUTH)
        hashSet.add(BaseConstants.CMD_LOGIN_CHANGEUIN_AUTH)
        hashSet.add("GrayUinPro.Check")
        hashSet.add(BaseConstants.CMD_WT_LOGIN_AUTH)
        hashSet.add(BaseConstants.CMD_WT_LOGIN_NAME2UIN)
        hashSet.add("wtlogin.exchange_emp")
        hashSet.add("wtlogin.trans_emp")
        hashSet.add("wtlogin.register")
        hashSet.add(BaseConstants.CMD_WT_LOGIN_ADDCONTACTS)
        hashSet.add(BaseConstants.CMD_WT_LOGIN_REQUESTREBINDMBL)
        hashSet.add(BaseConstants.CMD_CONNAUTHSVR_GETAPPINFO)
        hashSet.add(BaseConstants.CMD_CONNAUTHSVR_GETAUTHAPILIST)
        hashSet.add(BaseConstants.CMD_CONNAUTHSVR_GETAUTHAPI)
        hashSet.add("QQConnectLogin.pre_auth_emp")
        hashSet.add("QQConnectLogin.auth_emp")
        hashSet.add(BaseConstants.CMD_REQ_CHECKSIGNATURE)

        hashSet.add("trpc.o3.ecdh_access.EcdhAccess.SsoEstablishShareKey")
        hashSet.add("trpc.o3.ecdh_access.EcdhAccess.SsoSecureAccess")

        hashSet.add("trpc.o3.mobile_security.MobileSecurity.SsoCheckSwitch")
        hashSet.add(BaseConstants.CMD_REPORTSTAT)
        hashSet.add("trpc.qqlog.qqlog_push.Portal.SsoPullReportRule")
        hashSet.add("trpc.login.account_logic.AccountLogicService.SsoThirdPartQueryEncryptedBind")
        CodecWarpper.nativeInitNoLoginWhiteList(hashSet)
    }*/
}
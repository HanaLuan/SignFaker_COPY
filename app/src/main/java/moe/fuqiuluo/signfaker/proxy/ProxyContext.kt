package moe.fuqiuluo.signfaker.proxy

import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import android.view.Display
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import moe.fuqiuluo.signfaker.logger.TextLogger.log

class ProxyContext(
    private val myContext: Context,
    str: String = ""
): Application() {

    // 抄的别人代码,不一定靠谱
    /*~~Start~~*/
    private val contentResolver: ContentResolver
    private val fakePackageManager: PackageManager
    private val fekitDir: File
    private val realFilesDir: String
    private val realLibDir: String

    init {
        val resolve = File(myContext.cacheDir, "fekit")
        this.fekitDir = resolve
        val resolve2 = File(
            if (str.isBlank()) { File(File(resolve, "data"), str ) }
            else { File(resolve, "data") },
            "files"
        )
        resolve2.deleteRecursively()
        File(resolve2, "5463306EE50FE3AA").mkdirs()
        this.realFilesDir = resolve2.absolutePath
        this.realLibDir = myContext.applicationInfo.nativeLibraryDir
        this.fakePackageManager = myContext.packageManager
        this.contentResolver = myContext.contentResolver
    }

    private fun getFakeApplicationInfo(applicationInfo: ApplicationInfo, realLibDir: String): ApplicationInfo {
        val proxyApplicationInfo = ProxyApplicationInfo(ApplicationInfo())
        proxyApplicationInfo.packageName = applicationInfo.packageName
        proxyApplicationInfo.className = applicationInfo.className
        proxyApplicationInfo.targetSdkVersion = 31
        proxyApplicationInfo.nativeLibraryDir = realLibDir
        proxyApplicationInfo.flags = applicationInfo.flags and (-3)
        log(String.format(
            "FakePackageName = %s\nFakeClassName = %s\nFakeTargetSdkVersion = %s\nFakeNativeLibraryDir = %s",
            applicationInfo.packageName,
            applicationInfo.className,
            proxyApplicationInfo.targetSdkVersion,
            realFilesDir)
        )
        return proxyApplicationInfo
    }
    /*~~End~~*/

    override fun getAssets(): AssetManager {
        log("getAssets()")
        return myContext.assets
    }

    override fun getResources(): Resources {
        log("getResources() => Real")
        return myContext.resources
    }

    override fun getPackageManager(): PackageManager {
        log("getPackageManager() => Fake")
        return ProxyPackageManager(myContext.packageManager)
    }

    override fun getContentResolver(): ContentResolver {
        log("getContentResolver()")
        return myContext.contentResolver
    }

    override fun getMainLooper(): Looper {
        log("ml")
        return myContext.mainLooper
    }

    override fun getApplicationContext(): Context {
        log("getApplicationContext => this")
        return this
    }

    override fun setTheme(p0: Int) {
        log("Proxy => setTheme($p0)")
        setTheme(p0)
    }

    override fun getTheme(): Resources.Theme {
        log("Proxy => getTheme() -> ${myContext.theme}")
        return myContext.theme
    }

    override fun getClassLoader(): ClassLoader {
        log("Proxy => getClassLoader() -> ${myContext.classLoader}")
        return myContext.classLoader
    }

    override fun getPackageName(): String {
        log("getPackageName() => \"com.tencent.mobileqq\"")
        return "com.tencent.mobileqq"
    }

    override fun getApplicationInfo(): ApplicationInfo {
//        log("Proxy => getApplicationInfo() from ProxyApplicationInfo -> ${myContext.applicationInfo}")
//        return ProxyApplicationInfo(myContext.applicationInfo)
//    }
        val applicationInfo = getFakeApplicationInfo(myContext.applicationInfo, this.realLibDir)
        log("Proxy => getApplicationInfo() from ProxyApplicationInfo -> $applicationInfo")
        return applicationInfo
    }

    override fun getPackageResourcePath(): String {
        log("Proxy => getPackageResourcePath() -> ${myContext.packageResourcePath}")
        return myContext.packageResourcePath
    }

    override fun getPackageCodePath(): String {
        log("pcp")
        return myContext.packageCodePath
    }

    override fun getSharedPreferences(p0: String?, p1: Int): SharedPreferences {
        log("getSharedPreferences($p0)")
        return myContext.getSharedPreferences(p0, p1)
    }

    override fun moveSharedPreferencesFrom(p0: Context?, p1: String?): Boolean {
        log("mspf")
        return moveSharedPreferencesFrom(p0, p1)
    }

    override fun deleteSharedPreferences(p0: String?): Boolean {
        log("dsp")
        return myContext.deleteSharedPreferences(p0)
    }

    override fun openFileInput(p0: String?): FileInputStream {
        log("ofi")
        return myContext.openFileInput(p0)
    }

    override fun openFileOutput(p0: String?, p1: Int): FileOutputStream {
        log("ofo")
        return myContext.openFileOutput(p0, p1)
    }

    override fun deleteFile(p0: String?): Boolean {
        log("df")
        return myContext.deleteFile(p0)
    }

    override fun getFileStreamPath(p0: String?): File {
        log("gfp")
        return myContext.getFileStreamPath(p0)
    }

    override fun getDataDir(): File {
        log("dr")
        return myContext.dataDir
    }

    fun getFilesDirV2(): File {
        return myContext.filesDir
    }

    override fun getFilesDir(): File {
        log("getFilesDir() => ${myContext.filesDir}")
        return myContext.filesDir
    }

    override fun getNoBackupFilesDir(): File {
        log("nbfd")
        return myContext.noBackupFilesDir
    }

    override fun getExternalFilesDir(p0: String?): File? {
        log("gefd")
        return myContext.getExternalFilesDir(p0)
    }

    override fun getExternalFilesDirs(p0: String?): Array<File> {
        log("gefds")
        return myContext.getExternalFilesDirs(p0)
    }

    override fun getObbDir(): File {
        log("od")
        return myContext.obbDir
    }

    override fun getObbDirs(): Array<File> {
        log("ods")
        return myContext.obbDirs
    }

    override fun getCacheDir(): File {
        log("cd")
        return myContext.cacheDir
    }

    override fun getCodeCacheDir(): File {
        TODO("Not yet implemented")
    }

    override fun getExternalCacheDir(): File? {
        TODO("Not yet implemented")
    }

    override fun getExternalCacheDirs(): Array<File> {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun getExternalMediaDirs(): Array<File> {
        TODO("Not yet implemented")
    }

    override fun fileList(): Array<String> {
        TODO("Not yet implemented")
    }

    override fun getDir(p0: String?, p1: Int): File {
        TODO("Not yet implemented")
    }

    override fun openOrCreateDatabase(
        p0: String?,
        p1: Int,
        p2: SQLiteDatabase.CursorFactory?
    ): SQLiteDatabase {
        TODO("Not yet implemented")
    }

    override fun openOrCreateDatabase(
        p0: String?,
        p1: Int,
        p2: SQLiteDatabase.CursorFactory?,
        p3: DatabaseErrorHandler?
    ): SQLiteDatabase {
        TODO("Not yet implemented")
    }

    override fun moveDatabaseFrom(p0: Context?, p1: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun deleteDatabase(p0: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun getDatabasePath(p0: String?): File {
        log("getDatabasePath($p0)")
        return myContext.getDatabasePath(p0)
    }

    override fun databaseList(): Array<String> {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun getWallpaper(): Drawable {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun peekWallpaper(): Drawable {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun getWallpaperDesiredMinimumWidth(): Int {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun getWallpaperDesiredMinimumHeight(): Int {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun setWallpaper(p0: Bitmap?) {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun setWallpaper(p0: InputStream?) {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun clearWallpaper() {
        TODO("Not yet implemented")
    }

    override fun startActivity(p0: Intent?) {
        TODO("Not yet implemented")
    }

    override fun startActivity(p0: Intent?, p1: Bundle?) {
        TODO("Not yet implemented")
    }

    override fun startActivities(p0: Array<out Intent>?) {
        TODO("Not yet implemented")
    }

    override fun startActivities(p0: Array<out Intent>?, p1: Bundle?) {
        TODO("Not yet implemented")
    }

    override fun startIntentSender(p0: IntentSender?, p1: Intent?, p2: Int, p3: Int, p4: Int) {
        TODO("Not yet implemented")
    }

    override fun startIntentSender(
        p0: IntentSender?,
        p1: Intent?,
        p2: Int,
        p3: Int,
        p4: Int,
        p5: Bundle?
    ) {
        TODO("Not yet implemented")
    }

    override fun sendBroadcast(p0: Intent?) {
        TODO("Not yet implemented")
    }

    override fun sendBroadcast(p0: Intent?, p1: String?) {
        TODO("Not yet implemented")
    }

    override fun sendOrderedBroadcast(p0: Intent?, p1: String?) {
        TODO("Not yet implemented")
    }

    override fun sendOrderedBroadcast(
        p0: Intent,
        p1: String?,
        p2: BroadcastReceiver?,
        p3: Handler?,
        p4: Int,
        p5: String?,
        p6: Bundle?
    ) {
        TODO("Not yet implemented")
    }

    override fun sendBroadcastAsUser(p0: Intent?, p1: UserHandle?) {
        TODO("Not yet implemented")
    }

    override fun sendBroadcastAsUser(p0: Intent?, p1: UserHandle?, p2: String?) {
        TODO("Not yet implemented")
    }

    override fun sendOrderedBroadcastAsUser(
        p0: Intent?,
        p1: UserHandle?,
        p2: String?,
        p3: BroadcastReceiver?,
        p4: Handler?,
        p5: Int,
        p6: String?,
        p7: Bundle?
    ) {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java", ReplaceWith("TODO(\"Not yet implemented\")"))
    override fun sendStickyBroadcast(p0: Intent?) {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java", ReplaceWith("TODO(\"Not yet implemented\")"))
    override fun sendStickyOrderedBroadcast(
        p0: Intent?,
        p1: BroadcastReceiver?,
        p2: Handler?,
        p3: Int,
        p4: String?,
        p5: Bundle?
    ) {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java", ReplaceWith("TODO(\"Not yet implemented\")"))
    override fun removeStickyBroadcast(p0: Intent?) {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java", ReplaceWith("TODO(\"Not yet implemented\")"))
    override fun sendStickyBroadcastAsUser(p0: Intent?, p1: UserHandle?) {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java", ReplaceWith("TODO(\"Not yet implemented\")"))
    override fun sendStickyOrderedBroadcastAsUser(
        p0: Intent?,
        p1: UserHandle?,
        p2: BroadcastReceiver?,
        p3: Handler?,
        p4: Int,
        p5: String?,
        p6: Bundle?
    ) {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java", ReplaceWith("TODO(\"Not yet implemented\")"))
    override fun removeStickyBroadcastAsUser(p0: Intent?, p1: UserHandle?) {
        TODO("Not yet implemented")
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun registerReceiver(receiver: BroadcastReceiver?, intent: IntentFilter): Intent? {
        log("registerReceiver(${receiver.toString()}, $intent)")
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            myContext.registerReceiver(receiver, intent, RECEIVER_NOT_EXPORTED)
        } else {
            myContext.registerReceiver(receiver, intent)
        }
    }

    override fun registerReceiver(p0: BroadcastReceiver?, p1: IntentFilter?, p2: Int): Intent? {
        TODO("Not yet implemented")
    }

    override fun registerReceiver(
        p0: BroadcastReceiver?,
        p1: IntentFilter?,
        p2: String?,
        p3: Handler?
    ): Intent? {
        TODO("Not yet implemented")
    }

    override fun registerReceiver(
        p0: BroadcastReceiver?,
        p1: IntentFilter?,
        p2: String?,
        p3: Handler?,
        p4: Int
    ): Intent? {
        TODO("Not yet implemented")
    }

    override fun unregisterReceiver(p0: BroadcastReceiver?) {
        TODO("Not yet implemented")
    }

    override fun startService(p0: Intent?): ComponentName? {
        TODO("Not yet implemented")
    }

    override fun startForegroundService(p0: Intent?): ComponentName? {
        TODO("Not yet implemented")
    }

    override fun stopService(p0: Intent?): Boolean {
        TODO("Not yet implemented")
    }

    override fun bindService(p0: Intent, p1: ServiceConnection, p2: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun unbindService(p0: ServiceConnection) {
        TODO("Not yet implemented")
    }

    override fun startInstrumentation(p0: ComponentName, p1: String?, p2: Bundle?): Boolean {
        TODO("Not yet implemented")
    }

    override fun getSystemService(name: String): Any {
        log("getSystemService($name)")
        return myContext.getSystemService(name)
    }

    override fun getSystemServiceName(p0: Class<*>): String? {
        TODO("Not yet implemented")
    }

    override fun checkPermission(p0: String, p1: Int, p2: Int): Int {
        TODO("Not yet implemented")
    }

    override fun checkCallingPermission(p0: String): Int {
        TODO("Not yet implemented")
    }

    override fun checkCallingOrSelfPermission(p0: String): Int {
        TODO("Not yet implemented")
    }

    override fun checkSelfPermission(p0: String): Int {
        TODO("Not yet implemented")
    }

    override fun enforcePermission(p0: String, p1: Int, p2: Int, p3: String?) {
        TODO("Not yet implemented")
    }

    override fun enforceCallingPermission(p0: String, p1: String?) {
        TODO("Not yet implemented")
    }

    override fun enforceCallingOrSelfPermission(p0: String, p1: String?) {
        TODO("Not yet implemented")
    }

    override fun grantUriPermission(p0: String?, p1: Uri?, p2: Int) {
        TODO("Not yet implemented")
    }

    override fun revokeUriPermission(p0: Uri?, p1: Int) {
        TODO("Not yet implemented")
    }

    override fun revokeUriPermission(p0: String?, p1: Uri?, p2: Int) {
        TODO("Not yet implemented")
    }

    override fun checkUriPermission(p0: Uri?, p1: Int, p2: Int, p3: Int): Int {
        TODO("Not yet implemented")
    }

    override fun checkUriPermission(
        p0: Uri?,
        p1: String?,
        p2: String?,
        p3: Int,
        p4: Int,
        p5: Int
    ): Int {
        TODO("Not yet implemented")
    }

    override fun checkCallingUriPermission(p0: Uri?, p1: Int): Int {
        TODO("Not yet implemented")
    }

    override fun checkCallingOrSelfUriPermission(p0: Uri?, p1: Int): Int {
        TODO("Not yet implemented")
    }

    override fun enforceUriPermission(p0: Uri?, p1: Int, p2: Int, p3: Int, p4: String?) {
        log("enforceUriPermission")
        return myContext.enforceUriPermission(p0, p1, p2, p3, p4)
    }

    override fun enforceUriPermission(
        p0: Uri?,
        p1: String?,
        p2: String?,
        p3: Int,
        p4: Int,
        p5: Int,
        p6: String?
    ) {
        log("enforceUriPermission")
        return myContext.enforceUriPermission(p0, p1, p2, p3, p4, p5, p6)
    }

    override fun enforceCallingUriPermission(p0: Uri?, p1: Int, p2: String?) {
        log("enforceCallingUriPermission")
        return myContext.enforceCallingUriPermission(p0, p1, p2)
    }

    override fun enforceCallingOrSelfUriPermission(p0: Uri?, p1: Int, p2: String?) {
        log("enforceCallingOrSelfUriPermission")
        return myContext.enforceCallingOrSelfUriPermission(p0, p1, p2)
    }

    override fun createPackageContext(p0: String?, p1: Int): Context {
        log("cpc")
        return myContext.createPackageContext(p0, p1)
    }

    @SuppressLint("ObsoleteSdkInt")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun createContextForSplit(p0: String?): Context {
        log("ccfs")
        return myContext.createContextForSplit(p0)
    }

    override fun createConfigurationContext(p0: Configuration): Context {
        log("ccc")
        return myContext.createConfigurationContext(p0)
    }

    override fun createDisplayContext(p0: Display): Context {
        log("cdc")
        return myContext.createDisplayContext(p0)
    }

    override fun createDeviceProtectedStorageContext(): Context {
        log("cdpsc")
        return myContext.createDeviceProtectedStorageContext()
    }

    override fun isDeviceProtectedStorage(): Boolean {
        log("idps")
        return myContext.isDeviceProtectedStorage
    }
}
import org.gradle.api.tasks.bundling.Jar
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
}

android {
    namespace = "moe.fuqiuluo.signfaker"
    compileSdk = 35

    defaultConfig {
        applicationId = "moe.hanahime.signfaker"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "1.0.1-foolNeko"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += "armeabi-v7a"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            ndk {
                abiFilters += "armeabi-v7a"
            }
        }
        debug {
            ndk {
                abiFilters += "armeabi-v7a"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    packaging {
        resources {
            excludes += listOf(
                "META-INF/INDEX.LIST",
                "META-INF/io.netty.versions.properties",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0",
                "META-INF/*.kotlin_module"
            )
        }
    }
}

fun setFileTimestamps(archive: Provider<RegularFile>, instant: Instant) {
    val dosTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC)
    val oldFile = archive.get().asFile
    val zipFile = ZipFile(oldFile)
    val newName = oldFile.name + ".new"
    val newFile = File(oldFile.parentFile, newName)
    val output = ZipOutputStream(FileOutputStream(newFile))
    zipFile.entries().asSequence().forEach { entry ->
        val clone = ZipEntry(entry)
        val input = zipFile.getInputStream(entry)
        clone.time = dosTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli()
        print(clone.time)
        output.putNextEntry(clone)
        input.transferTo(output)
        output.closeEntry()
        input.close()
    }
    output.close()
    zipFile.close()
    Files.move(newFile.toPath(), oldFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
}

var buildInstant = Instant.now().truncatedTo(ChronoUnit.SECONDS)
val sourceDateEpoch = System.getenv("SOURCE_DATE_EPOCH")
if (sourceDateEpoch != null) {
    val epochSeconds = sourceDateEpoch.toLong()
    buildInstant = Instant.ofEpochSecond(epochSeconds)
}


allprojects {
    // Normalizes the ZIP and JAR archives
    tasks.withType<Jar> {
        doLast {
            setFileTimestamps(archiveFile, buildInstant)
        }
    }
}


val ktor_version: String by project

dependencies {
    //implementation ("io.netty:netty-all:4.1.68.Final")
    annotationProcessor ("org.apache.logging.log4j:log4j-core:2.17.1")
    //implementation ("org.apache.logging.log4j:log4j-api:2.19.0")
    implementation ("org.eclipse.jetty:jetty-servlet:9.4.43.v20210629")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-host-common:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("org.bouncycastle:bcprov-jdk15on:1.70")

    implementation(files("libs/beacon_android_v3.2.0.3_official64_release.jar"))
    implementation("com.google.firebase:firebase-crashlytics-buildtools:3.0.2")
    implementation("com.tencent:mmkv:1.3.7")
    implementation("com.alibaba:fastjson:2.0.31")
    implementation("com.google.firebase:firebase-database-ktx:21.0.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.*

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    id("com.github.gmazzo.buildconfig") version "6.0.9"

    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}

// 1. Read the local.properties file
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

buildConfig {
    // Read from your existing local.properties logic
    val token = localProperties.getProperty("ANILIST_TOKEN") ?: "\"\""
    val malToken = localProperties.getProperty("MYANIMELIST_TOKEN") ?: "\"\""
    val malClientID = localProperties.getProperty("MAL_CLIENT_ID") ?: "\"\""

    // base url
    buildConfigField("String", "ANILIST_BASE_URL", localProperties.getProperty("ANILIST_BASE_URL") ?: "https://graphql.anilist.co")
    buildConfigField("String", "MAL_BASE_URL", localProperties.getProperty("MAL_BASE_URL") ?: "https://api.myanimelist.net/v2")
    buildConfigField("String", "JIKAN_BASE_URL", localProperties.getProperty("JIKAN_BASE_URL") ?: "https://api.jikan.moe/v4")

    buildConfigField("String", "ANILIST_TOKEN", token)
    buildConfigField("String", "MAL_TOKEN", malToken)
    buildConfigField("Boolean", "IS_DEBUG", true)

    buildConfigField("String", "ANILIST_CLIENT_ID", localProperties.getProperty("ANILIST_CLIENT_ID") ?: "")
    buildConfigField("String", "MAL_CLIENT_ID", localProperties.getProperty("MAL_CLIENT_ID") ?: "")

}

room {
    schemaDirectory("$projectDir/schemas")
}



kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm()

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.ktx)
            // Android Koin
            implementation(libs.koin.android)

            //ktor
            implementation(libs.ktor.client.okhttp)


            //room
            implementation(libs.androidx.room.sqlite.wrapper)

            //
            implementation(libs.androidx.security.crypto)


//            implementation(libs.multiplatform.settings.android)

        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)


            //coil3
            implementation(libs.coil3)
            implementation(libs.coil3.network.okhttp)

            //theme
            implementation(libs.materialKolor)

            //logger
            implementation(libs.napier)

            // Kotlinx Libraries
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)

            // Koin DI
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewModel)

            // Paging
            implementation(libs.androidx.paging.compose)
//            implementation(libs.androidx.paging.runtime)

            //icons
            implementation(libs.jetbrains.compose.icons)

            //ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)

            //settings
            implementation(libs.multiplatform.settings)

            //room
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.androidx.room.paging)
//            implementation(libs.androidx.room.ktx)


        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            //ktor
            implementation(libs.ktor.client.cio)

            implementation(libs.multiplatform.settings.jvm)
        }
    }
}



android {
    namespace = "com.ghost.zeku"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.ghost.zeku"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "com.ghost.zeku.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.ghost.zeku"
            packageVersion = "1.0.0"
        }
    }
}


dependencies {
    add("kspCommonMainMetadata", libs.androidx.room.compiler)
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspJvm", libs.androidx.room.compiler)
}
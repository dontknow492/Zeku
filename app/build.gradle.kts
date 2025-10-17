plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "org.ghost.zeku"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "org.ghost.zeku"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "0.7.9-beta"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Add this to tell Gradle which native libraries to package.
//        ndk {
////            abiFilters.addAll(
//////                listOf("x86", "x86_64", "armeabi-v7a", "arm64-v8a")
////            )
//        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            // Debug-specific configurations can go here
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    kotlin {
        jvmToolchain(17)
    }
    splits {
        abi {
            isEnable = true
            // This ensures each APK has a unique version code
            include("x86", "x86_64", "armeabi-v7a", "arm64-v8a")
            exclude("mips64","armeabi","riscv64","mips")
            isUniversalApk = false
        }
    }

}

dependencies {
    implementation(libs.bundles.compose.ui)
    implementation(libs.bundles.core)
    implementation(libs.bundles.room)
    implementation(libs.androidx.documentfile)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.ui)
    ksp(libs.androidx.room.compiler)
    implementation(libs.bundles.paging)
    implementation(libs.bundles.hilt)
    ksp(libs.hilt.compiler)
    implementation(libs.bundles.coil)
    implementation(libs.bundles.youtubedl)

    //lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)

    //testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.bundles.testing.android)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    //datastore
    implementation(libs.androidx.datastore.preferences)

    //json
    implementation(libs.kotlinx.serialization.json)

    //navigation
    implementation(libs.androidx.compose.navigation)

    //reorderable
    implementation(libs.compose.reorderable)

    //timber
    implementation(libs.timber)

    //materialKolor
    implementation(libs.materialKolor)
}
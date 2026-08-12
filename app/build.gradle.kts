import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.roborazzi)
    alias(libs.plugins.secrets)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example"

    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.aistudio.shaghaf.vids"

        minSdk = 24
        targetSdk = 36

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    /*
     * Release signing
     *
     * Release builds require a real keystore supplied through
     * GitHub Actions / Codemagic environment variables.
     */
    signingConfigs {
        create("release") {
            val keystorePath =
                System.getenv("KEYSTORE_PATH")
                    ?: "${rootDir}/my-upload-key.jks"

            storeFile = file(keystorePath)

            storePassword =
                System.getenv("STORE_PASSWORD")

            keyAlias = "upload"

            keyPassword =
                System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {

        /*
         * Release APK
         */
        release {
            isCrunchPngs = false
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )

            signingConfig =
                signingConfigs.getByName("release")
        }

        /*
         * Debug APK
         *
         * IMPORTANT:
         * Do NOT specify debug.keystore here.
         *
         * Android Gradle Plugin automatically creates/uses
         * the standard debug keystore on the build machine.
         */
        debug {
            // Default Android debug signing
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

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

/*
 * Secrets Gradle Plugin
 */
secrets {
    propertiesFileName = ".env"
    defaultPropertiesFileName = ".env.example"
}

/*
 * Google Services
 *
 * Missing google-services.json is treated as a warning
 * rather than an immediate configuration failure.
 */
googleServices {
    missingGoogleServicesStrategy =
        MissingGoogleServicesStrategy.WARN
}

/*
 * Dependencies
 */
dependencies {

    // Compose BOM
    implementation(
        platform(
            libs.androidx.compose.bom
        )
    )

    // Firebase BOM
    implementation(
        platform(
            libs.firebase.bom
        )
    )

    // AndroidX Activity
    implementation(
        libs.androidx.activity.compose
    )

    // Material Icons
    implementation(
        libs.androidx.compose.material.icons.core
    )

    implementation(
        libs.androidx.compose.material.icons.extended
    )

    // Material 3
    implementation(
        libs.androidx.compose.material3
    )

    // Compose UI
    implementation(
        libs.androidx.compose.ui
    )

    implementation(
        libs.androidx.compose.ui.graphics
    )

    implementation(
        libs.androidx.compose.ui.tooling.preview
    )

    // Core
    implementation(
        libs.androidx.core.ktx
    )

    // Lifecycle
    implementation(
        libs.androidx.lifecycle.runtime.compose
    )

    implementation(
        libs.androidx.lifecycle.runtime.ktx
    )

    implementation(
        libs.androidx.lifecycle.viewmodel.compose
    )

    // Navigation
    implementation(
        libs.androidx.navigation.compose
    )

    // Room
    implementation(
        libs.androidx.room.ktx
    )

    implementation(
        libs.androidx.room.runtime
    )

    // Coil
    implementation(
        libs.coil.compose
    )

    // Retrofit / Moshi
    implementation(
        libs.converter.moshi
    )

    implementation(
        libs.moshi.kotlin
    )

    implementation(
        libs.retrofit
    )

    implementation(
        libs.okhttp
    )

    implementation(
        libs.logging.interceptor
    )

    // Firebase AI
    implementation(
        libs.firebase.ai
    )

    // Firebase App Check
    implementation(
        libs.firebase.appcheck.recaptcha
    )

    // Coroutines
    implementation(
        libs.kotlinx.coroutines.android
    )

    implementation(
        libs.kotlinx.coroutines.core
    )

    // Room KSP
    "ksp"(
        libs.androidx.room.compiler
    )

    // Moshi KSP
    "ksp"(
        libs.moshi.kotlin.codegen
    )

    /*
     * Unit Tests
     */
    testImplementation(
        libs.androidx.compose.ui.test.junit4
    )

    testImplementation(
        libs.androidx.core
    )

    testImplementation(
        libs.androidx.junit
    )

    testImplementation(
        libs.junit
    )

    testImplementation(
        libs.kotlinx.coroutines.test
    )

    testImplementation(
        libs.robolectric
    )

    testImplementation(
        libs.roborazzi
    )

    testImplementation(
        libs.roborazzi.compose
    )

    testImplementation(
        libs.roborazzi.junit.rule
    )

    /*
     * Android Tests
     */
    androidTestImplementation(
        platform(
            libs.androidx.compose.bom
        )
    )

    androidTestImplementation(
        libs.androidx.compose.ui.test.junit4
    )

    androidTestImplementation(
        libs.androidx.espresso.core
    )

    androidTestImplementation(
        libs.androidx.junit
    )

    androidTestImplementation(
        libs.androidx.runner
    )

    /*
     * Debug dependencies
     */
    debugImplementation(
        libs.androidx.compose.ui.test.manifest
    )

    debugImplementation(
        libs.androidx.compose.ui.tooling
    )
}

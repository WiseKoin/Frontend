import java.net.URI

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

fun String.asBuildConfigString(): String =
    "\"${replace("\\", "\\\\").replace("\"", "\\\"")}\""

fun requireValidApiBaseUrl(name: String, value: String): String {
    val uri = runCatching { URI(value) }
        .getOrElse { throw GradleException("$name must be a valid absolute URL.", it) }
    if (uri.scheme != "https" || uri.host.isNullOrBlank() || !value.endsWith('/')) {
        throw GradleException("$name must be an HTTPS URL ending with '/'.")
    }
    return value
}

val debugApiBaseUrl = providers.gradleProperty("WISEKOIN_DEBUG_API_BASE_URL")
    .orElse(providers.environmentVariable("WISEKOIN_DEBUG_API_BASE_URL"))
    .getOrElse("http://10.0.2.2:8080/")

val configuredReleaseApiBaseUrl = providers.gradleProperty("WISEKOIN_API_BASE_URL")
    .orElse(providers.environmentVariable("WISEKOIN_API_BASE_URL"))
    .orNull
val releaseBuildRequested = gradle.startParameter.taskNames.any {
    it.contains("release", ignoreCase = true)
}
val releaseApiBaseUrl = when {
    configuredReleaseApiBaseUrl != null ->
        requireValidApiBaseUrl("WISEKOIN_API_BASE_URL", configuredReleaseApiBaseUrl)
    releaseBuildRequested ->
        throw GradleException(
            "WISEKOIN_API_BASE_URL is required for release builds and must be an HTTPS URL ending with '/'."
        )
    else -> "https://invalid.local/"
}

android {
    namespace = "com.wisekoin"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.wisekoin"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", debugApiBaseUrl.asBuildConfigString())
        }
        release {
            buildConfigField("String", "API_BASE_URL", releaseApiBaseUrl.asBuildConfigString())
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("io.coil-kt:coil-compose:2.7.0")

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Retrofit & Gson Converter
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coil (Presenting image)
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Camera
    val cameraxVersion = "1.3.4"
    implementation("androidx.camera:camera-core:${cameraxVersion}")
    implementation("androidx.camera:camera-camera2:${cameraxVersion}")
    implementation("androidx.camera:camera-lifecycle:${cameraxVersion}")
    implementation("androidx.camera:camera-view:${cameraxVersion}")

    // Font
    implementation("androidx.compose.ui:ui-text-google-fonts:1.6.8")

    // Core library desugaring (java.time on minSdk < 26)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}

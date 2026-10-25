import io.sentry.android.gradle.extensions.InstrumentationFeature
import java.net.URI

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.sentry.android.gradle)
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

fun requireValidSentryDsn(value: String): String {
    val uri = runCatching { URI(value) }
        .getOrElse { throw GradleException("SENTRY_DSN must be a valid absolute URL.", it) }
    if (
        uri.scheme != "https" ||
        uri.host.isNullOrBlank() ||
        uri.userInfo.isNullOrBlank() ||
        uri.path.isNullOrBlank() ||
        uri.path == "/"
    ) {
        throw GradleException("SENTRY_DSN must be a valid HTTPS Sentry project DSN.")
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

val configuredSentryDsn = providers.gradleProperty("SENTRY_DSN")
    .orElse(providers.environmentVariable("SENTRY_DSN"))
    .orNull
    ?.trim()
    ?.takeIf(String::isNotEmpty)
val sentryDsn = when {
    configuredSentryDsn != null ->
        requireValidSentryDsn(configuredSentryDsn)
    releaseBuildRequested ->
        throw GradleException("SENTRY_DSN is required for release builds.")
    else -> ""
}
val sentryAuthToken = providers.gradleProperty("SENTRY_AUTH_TOKEN")
    .orElse(providers.environmentVariable("SENTRY_AUTH_TOKEN"))
    .orNull
    ?.trim()
    ?.takeIf(String::isNotEmpty)

data class ReleaseSigningValues(
    val storeFile: String?,
    val storePassword: String?,
    val keyAlias: String?,
    val keyPassword: String?
) {
    val anyConfigured = listOf(storeFile, storePassword, keyAlias, keyPassword).any { !it.isNullOrBlank() }
    val fullyConfigured = listOf(storeFile, storePassword, keyAlias, keyPassword).all { !it.isNullOrBlank() }
}

fun secret(name: String): String? = providers.gradleProperty(name)
    .orElse(providers.environmentVariable(name))
    .orNull

val releaseSigning = ReleaseSigningValues(
    storeFile = secret("WISEKOIN_RELEASE_STORE_FILE"),
    storePassword = secret("WISEKOIN_RELEASE_STORE_PASSWORD"),
    keyAlias = secret("WISEKOIN_RELEASE_KEY_ALIAS"),
    keyPassword = secret("WISEKOIN_RELEASE_KEY_PASSWORD")
)

if (releaseSigning.anyConfigured && !releaseSigning.fullyConfigured) {
    throw GradleException(
        "Release signing is partially configured. Set all WISEKOIN_RELEASE_* signing values."
    )
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
        manifestPlaceholders["sentryDsn"] = sentryDsn
    }

    signingConfigs {
        if (releaseSigning.fullyConfigured) {
            create("release") {
                storeFile = file(requireNotNull(releaseSigning.storeFile))
                storePassword = releaseSigning.storePassword
                keyAlias = releaseSigning.keyAlias
                keyPassword = releaseSigning.keyPassword
            }
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", debugApiBaseUrl.asBuildConfigString())
            manifestPlaceholders["sentryEnvironment"] = "development"
            manifestPlaceholders["sentryTracesSampleRate"] = "1.0"
        }
        release {
            buildConfigField("String", "API_BASE_URL", releaseApiBaseUrl.asBuildConfigString())
            manifestPlaceholders["sentryEnvironment"] = "production"
            manifestPlaceholders["sentryTracesSampleRate"] = "0.1"
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.findByName("release")
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

sentry {
    org = "thien-phuc-tran"
    projectName = "wisekoin-android"
    authToken = sentryAuthToken

    includeProguardMapping = true
    autoUploadProguardMapping = sentryAuthToken != null
    includeSourceContext = sentryAuthToken != null
    includeDependenciesReport = false
    telemetry = false

    tracingInstrumentation {
        enabled = true
        features = setOf(
            InstrumentationFeature.FILE_IO,
            InstrumentationFeature.OKHTTP,
            InstrumentationFeature.COMPOSE
        )
        logcat {
            enabled = false
        }
    }

    autoInstallation {
        enabled = true
        sentryVersion = libs.versions.sentry.get()
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation(libs.coil.compose)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Retrofit & Gson Converter
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

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

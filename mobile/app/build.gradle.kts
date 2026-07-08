plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.compose)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("io.realm.kotlin")
    alias(libs.plugins.compose.screenshot)
}

android {
    namespace = "com.catalinalabs.reeler"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.catalinalabs.reeler"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Set in gradle.properties (or ~/.gradle/gradle.properties) as
        // clerkPublishableKey=pk_test_... to enable the premium features.
        val clerkPublishableKey = project.findProperty("clerkPublishableKey") as? String ?: ""
        buildConfigField("String", "CLERK_PUBLISHABLE_KEY", "\"$clerkPublishableKey\"")

        // Clerk-hosted checkout/billing page opened by the Subscribe button,
        // e.g. your Clerk Account Portal user profile billing URL.
        val premiumCheckoutUrl = project.findProperty("premiumCheckoutUrl") as? String ?: ""
        buildConfigField("String", "PREMIUM_CHECKOUT_URL", "\"$premiumCheckoutUrl\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    @Suppress("UnstableApiUsage")
    experimentalProperties["android.experimental.enableScreenshotTest"] = true
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

val ktor_version: String by project

configurations.all {
    resolutionStrategy {
        // The Clerk SDK pulls kotlin-stdlib/kotlinx libraries built with
        // Kotlin 2.2+, which our Kotlin 2.0.x compiler (pinned by
        // realm-kotlin) cannot read. Force versions with 2.0-compatible
        // metadata.
        force("org.jetbrains.kotlin:kotlin-stdlib:2.0.20")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk7:2.0.20")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.0.20")
        force("org.jetbrains.kotlinx:kotlinx-serialization-core:1.7.3")
        force("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
        force("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
        force("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.coil.compose)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    screenshotTestImplementation(libs.androidx.ui.tooling)
    screenshotTestImplementation(libs.screenshot.validation.api)

    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")

    implementation(libs.play.services.ads)

    implementation(libs.hilt.android)
    implementation(libs.hilt.work)
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)

    implementation(libs.jsoup)
    implementation(libs.java.youtube.downloader)

    implementation(libs.library.base)

    implementation(libs.clerk.android.api)
}
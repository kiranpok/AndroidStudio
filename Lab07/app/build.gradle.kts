plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.lab07"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.lab07"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    // Core Kotlin and Lifecycle components
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Compose-related dependencies
    implementation(libs.androidx.activity.compose) // For Compose activity
    implementation(platform(libs.androidx.compose.bom)) // Compose BOM
    implementation("androidx.compose.ui:ui") // Jetpack Compose UI
    implementation("androidx.compose.material3:material3") // Material 3 for Surface, Button, etc.
    implementation("androidx.compose.ui:ui-tooling-preview") // Preview in Android Studio
    implementation("androidx.compose.ui:ui-graphics") // Graphics support in Compose

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4") // UI testing in Compose

    // Debug tooling for Compose
    debugImplementation("androidx.compose.ui:ui-tooling") // UI tooling for debugging
    debugImplementation("androidx.compose.ui:ui-test-manifest") // For Compose test manifests
}

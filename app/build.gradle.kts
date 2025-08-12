plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("plugin.serialization") version "2.1.0" // match your Kotlin version
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
    id("androidx.room")
    // Apply the Kotlin Annotation Processing Tool (KAPT) plugin
    id("org.jetbrains.kotlin.kapt") // Add this line
}

android {
    namespace = "it.cynomys.cfmandroid"
    compileSdk = 35

    defaultConfig {
        applicationId = "it.cynomys.cfmandroid"
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

    // --- START: Added Room schema directory configuration ---
    room {
        // The schemas directory contains a schema file for each version of the Room database.
        // This is required to enable Room to validate the database and generate migrations.
        schemaDirectory("$projectDir/schemas")
    }
    // --- END: Added Room schema directory configuration ---
}

dependencies {

    //Room databse

    val room_version = "2.7.1"

    implementation("androidx.room:room-runtime:$room_version")
    // Change from annotationProcessor to kapt for Kotlin projects
    kapt("androidx.room:room-compiler:$room_version") // For Kotlin

    // WorkManager
    val work_version = "2.9.0" // Use the latest stable version
    implementation("androidx.work:work-runtime-ktx:$work_version")

    // Retrofit for network requests
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0") // For JSON parsing
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3") // Optional: For logging network requests
    implementation("androidx.navigation:navigation-compose:2.8.7")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation ("androidx.compose.material:material-icons-extended:1.7.8" ) // For icons
// Media3 ExoPlayer
    implementation ("androidx.media3:media3-exoplayer:1.1.1")
    implementation ("androidx.media3:media3-ui:1.1.1")
    implementation ("androidx.webkit:webkit:1.8.0")

    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

// CameraX Core
    implementation ("androidx.camera:camera-core:1.3.0")
    implementation ("androidx.camera:camera-camera2:1.3.0")
    implementation ("androidx.camera:camera-lifecycle:1.3.0")
    implementation ("androidx.camera:camera-view:1.3.0") // Includes PreviewView
    implementation ("com.google.code.gson:gson:2.10.1") // Or the latest stable version

    implementation ("org.osmdroid:osmdroid-android:6.1.17")

    implementation ("com.google.mlkit:barcode-scanning:17.2.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.room.common.jvm)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

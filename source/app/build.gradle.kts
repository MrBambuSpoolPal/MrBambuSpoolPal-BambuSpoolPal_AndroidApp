// 1. Apply necessary plugins
plugins {
    // Apply the Android Application plugin to build an Android application.
    alias(libs.plugins.android.application)
    // Apply the Kotlin Android plugin to enable Kotlin support for Android development.
    alias(libs.plugins.kotlin.android)
    // Apply the Kotlin Compose plugin to enable Jetpack Compose support.
    alias(libs.plugins.kotlin.compose)
    // Apply the Kotlin Symbol Processing (KSP) plugin for annotation processing.
    alias(libs.plugins.ksp)
}

// 2. Android configurations
android {
    // Set the namespace for the application.
    namespace = "app.mrb.bambuspoolpal"
    // Set the compile SDK version.
    compileSdk = 35

    defaultConfig {
        // Set the application ID.
        applicationId = "app.mrb.bambuspoolpal"
        // Set the minimum SDK version.
        minSdk = 29
        // Set the target SDK version.
        targetSdk = 35
        // Set the version code.
        versionCode = 1
        // Set the version name.
        versionName = "1.0"

        // Set the test instrumentation runner.
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        // Enable the buildConfig generation.
        buildConfig = true
    }

    buildTypes {
        release {
            // Disable minification for release builds.
            isMinifyEnabled = false

            // Set the ProGuard rules for release builds.
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Define BuildConfig fields for the release version.
            // "VERSION_NAME" is set to "1.0.0" for the release build.
            buildConfigField("String", "VERSION_NAME", "\"1.0.0\"")

            // Add a "IS_DEBUG" field to BuildConfig with value false for the release version.
            // This field will help to know that we are in release mode.
            buildConfigField("boolean", "IS_DEBUG", "false")
        }

        debug {
            // Define BuildConfig fields for the debug version.
            // "VERSION_NAME" is set to "1.0.0-debug" for the debug build.
            buildConfigField("String", "VERSION_NAME", "\"1.0.0-debug\"")

            // Add a "IS_DEBUG" field to BuildConfig with value true for the debug version.
            // This field will help to know that we are in debug mode.
            buildConfigField("boolean", "IS_DEBUG", "true")
        }
    }


    compileOptions {
        // Set the source compatibility to Java 17.
        sourceCompatibility = JavaVersion.VERSION_17
        // Set the target compatibility to Java 17.
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        // Set the JVM target to Java 17.
        jvmTarget = "17"
    }

    buildFeatures {
        // Enable Jetpack Compose support.
        compose = true
    }

    composeOptions {
        // Set the Kotlin Compiler Extension version for Compose.
        kotlinCompilerExtensionVersion = "1.6.0"
    }
}

// 3. Define dependencies
dependencies {
    // Core Android & Kotlin dependencies.
    implementation(libs.androidx.core.ktx) // Android KTX extension library
    implementation(libs.androidx.appcompat) // AppCompat for backward compatibility
    implementation(libs.androidx.lifecycle.runtime.ktx) // Lifecycle KTX support

    // Jetpack Compose dependencies.
    implementation(platform(libs.androidx.compose.bom)) // Jetpack Compose BOM (Bill of Materials)
    implementation(libs.androidx.ui) // Jetpack Compose UI
    implementation(libs.androidx.ui.graphics) // Jetpack Compose Graphics
    implementation(libs.androidx.ui.geometry) // Added for CornerRadius
    implementation(libs.androidx.ui.unit) // Jetpack Compose Unit for layouts
    implementation(libs.androidx.ui.text) // Jetpack Compose Text
    implementation(libs.androidx.ui.tooling.preview) // Jetpack Compose Preview tooling
    implementation(libs.androidx.material3) // Jetpack Compose Material3
    implementation(libs.material.icons.extended) // Extended Material icons
    implementation(libs.androidx.runtime.livedata) // LiveData support for Compose
    implementation(libs.activity.compose) // Compose integration for Activities
    implementation(libs.lifecycle.viewmodel.compose) // ViewModel integration for Compose
    implementation(libs.androidx.navigation.compose) // Navigation integration for Compose

    // Network & Serialization dependencies.
    implementation(libs.retrofit) // Retrofit for networking
    implementation(libs.converterMoshi) // Moshi converter for Retrofit
    implementation(libs.moshi) // Moshi for JSON serialization
    implementation(libs.moshiKotlin) // Moshi Kotlin support
    implementation(libs.gson) // Gson for JSON parsing
    implementation(libs.retrofit.gson) // Gson converter for Retrofit

    // Security and ML dependencies.
    implementation(libs.bcprov.jdk15to18) // Bouncy Castle security provider
    implementation(libs.mlkit.text.recognition) // ML Kit Text Recognition
    implementation(libs.androidx.camera.core) // CameraX core library
    implementation(libs.androidx.camera.lifecycle) // CameraX lifecycle integration
    implementation(libs.androidx.camera.view) // CameraX view support
    implementation(libs.core.ktx) // Core KTX extension library
    implementation(libs.androidx.core.animation) // Animation extensions for Core KTX

    // KSP Annotation Processor dependency.
    ksp(libs.moshi.kotlin.codegen) // KSP code generation for Moshi

    // Testing dependencies.
    testImplementation(libs.junit) // JUnit for unit tests
    androidTestImplementation(libs.androidx.junit) // Android JUnit testing support
    androidTestImplementation(libs.androidx.espresso.core) // Espresso for UI testing
    androidTestImplementation(platform(libs.androidx.compose.bom)) // Compose testing BOM
    androidTestImplementation(libs.androidx.ui.test.junit4) // UI testing with Compose

    // Debugging dependencies.
    debugImplementation(libs.androidx.ui.tooling) // Compose tooling for debugging
    debugImplementation(libs.androidx.ui.test.manifest) // UI test manifest support

    // CameraX dependencies for advanced camera features.
    implementation(libs.androidx.camera.core)
    implementation(libs.camerax.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Jetpack Compose animation dependencies.
    implementation(libs.androidx.compose.animation.core) // Animation support for Compose
}

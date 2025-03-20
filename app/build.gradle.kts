plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.wave"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.wave"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["appAuthRedirectScheme"] = "https"
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

        buildFeatures {
            viewBinding = true
        }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.lottie)
    implementation(libs.firebase.auth)
    implementation(libs.espresso.intents)
    implementation(libs.fragment.testing)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.fragment.testing)
    implementation(libs.espresso.intents)
    implementation(libs.espresso.contrib)
    implementation(libs.firebase.firestore) {
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }
    testImplementation(libs.junit)
    testImplementation(libs.ext.junit)
    testImplementation(libs.junit.jupiter)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.core.ktx)
    implementation(libs.firebase.analytics) {
        exclude(group = "com.google.android.gms", module = "play-services-measurement-api")
    }
    implementation (libs.play.services.auth.v2070){
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }
    coreLibraryDesugaring (libs.desugar.jdk.libs)
    implementation ("com.google.android.material:material:1.9.0")
    implementation (libs.recyclerview)
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.logging.interceptor)
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")
    implementation(libs.facebook.login)
    implementation ("com.facebook.android:facebook-android-sdk:16.3.0")
    testImplementation("org.mockito:mockito-core:5.0.0")
    androidTestImplementation("org.mockito:mockito-android:5.0.0")
    implementation (libs.logging.interceptor.v493)
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.material.v1120)
    implementation("com.google.protobuf:protobuf-javalite:3.25.1")
    implementation ("com.google.firebase:firebase-storage:20.3.0")
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "com.google.protobuf" && requested.name == "protobuf-lite") {
            useTarget("com.google.protobuf:protobuf-javalite:3.25.1") // ✅ Correct syntax
        }
    }
}
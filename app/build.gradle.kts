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
    testImplementation(libs.junit)
    testImplementation(libs.ext.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.core.ktx)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation (libs.play.services.auth.v2070)
    coreLibraryDesugaring (libs.desugar.jdk.libs)
    implementation ("com.google.android.material:material:1.9.0")
    implementation (libs.recyclerview)
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.logging.interceptor)
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")
    testImplementation("org.mockito:mockito-core:5.0.0")
    androidTestImplementation("org.mockito:mockito-android:5.0.0")
    implementation (libs.logging.interceptor.v493)
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.material.v1120)
}
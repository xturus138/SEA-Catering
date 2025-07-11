plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("kotlin-kapt")
    id("kotlin-parcelize")

}

android {
    namespace = "com.example.seacatering"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.seacatering"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    implementation("com.google.android.gms:play-services-wallet:18.0.0")
    implementation(libs.glide)
    implementation("com.google.android.gms:play-services-location:21.3.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.google.play.services.auth)
    implementation(libs.googleid)
    implementation (libs.androidx.fragment.ktx)
    implementation (libs.androidx.lifecycle.viewmodel.ktx)
    implementation(platform(libs.google.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation (libs.android.lottie)
    implementation (libs.androidx.core.splashscreen)
    implementation(libs.material)
    implementation(libs.dotsindicator)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
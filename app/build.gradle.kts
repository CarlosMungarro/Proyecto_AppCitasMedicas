plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    // Añadidos para firebase
    id("com.google.gms.google-services")
}

android {
    namespace = "equipocitasmedicas.citasmedicas"
    compileSdk = 36

    defaultConfig {
        applicationId = "equipocitasmedicas.citasmedicas"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Glide para cargar imágenes
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation(libs.firebase.database)
    kapt("com.github.bumptech.glide:compiler:4.15.1")

    // Firebase BOM - Gestiona todas las versiones de Firebase automáticamente
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))

    // Firebase (sin especificar versión, el BOM las controla)
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    // ELIMINA ESTA LÍNEA: implementation("com.google.firebase:firebase-firestore-ktx:24.x.x")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
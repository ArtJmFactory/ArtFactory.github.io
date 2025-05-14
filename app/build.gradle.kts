// build.gradle.kts (Module: app) - CORRIGÉ

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    // --- AJOUT : Appliquer le plugin Kapt ici ---
    alias(libs.plugins.kotlin.kapt)
    // --- FIN AJOUT ---
}

android {
    namespace = "com.example.verbs1"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.verbs1"
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
    // kotlinOptions est utile même en Java pour le code généré par Kapt/Room
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.core.splashscreen)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.fragment:fragment:1.6.2") // Garde cette syntaxe si tu n'as pas d'alias pour fragment
    implementation(libs.core.ktx) // Ok

    // --- AJOUT : Dépendances Room ---
    implementation(libs.room.runtime) // Utilise l'alias du fichier TOML
    // IMPORTANT : Utilise 'kapt' car tu appliques le plugin kotlin-kapt
    kapt(libs.room.compiler) // Utilise l'alias du fichier TOML
    // --- FIN AJOUT ---

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
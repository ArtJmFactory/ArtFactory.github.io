// build.gradle.kts (Project: Verbs1) - NOUVELLE CORRECTION

// Le bloc 'plugins' déclare les plugins qui SERONT disponibles pour les modules.
// 'apply false' signifie qu'ils ne sont pas appliqués au projet racine lui-même.
plugins {
    // Alias pour le plugin Android Application (défini dans libs.versions.toml)
    alias(libs.plugins.android.application) apply false

    // Alias pour le plugin Kotlin Android (défini dans libs.versions.toml)
    alias(libs.plugins.jetbrains.kotlin.android) apply false

    // Alias pour le plugin Kotlin Kapt (défini dans libs.versions.toml)
    alias(libs.plugins.kotlin.kapt) apply false
}

// RIEN D'AUTRE DANS CE FICHIER (pas de dependencies {}, repositories {}, etc.)
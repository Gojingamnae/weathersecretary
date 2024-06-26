buildscript {
    dependencies {
        classpath(libs.google.services)
        classpath ("androidx.navigation:navigation-safe-args-gradle-plugin:2.8.0-beta01")
        classpath("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")
    }
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    id("com.google.gms.google-services") version "4.3.15" apply false
    id ("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")version "2.0.1"
}

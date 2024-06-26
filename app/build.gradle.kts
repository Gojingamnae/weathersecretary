plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
    id ("kotlin-parcelize")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
     id ("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.ilsa1000ri.weatherSecretary"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ilsa1000ri.weatherSecretary"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "1.0"

        buildConfigField("String", "PLACES_API_KEY", "\"AIzaSyA3VG-3vEqFJH-h163gkYc1-lqzm-CPd2o\"")
        buildConfigField("String", "MAPS_API_KEY", "\"AIzaSyCkyNvhYAGfiMQH5bOBasVTMvaNV1GbIGc\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        viewBinding = true
        compose = true
        dataBinding = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"  // Exclude the conflicting files
        }
    }
    viewBinding {
        enable = true
    }
}
secrets {
    // Optionally specify a different file name containing your secrets.
    // The plugin defaults to "local.properties"
    propertiesFileName = "secrets.properties"

    // A properties file containing default secret values. This file can be
    // checked in version control.
    defaultPropertiesFileName = "local.defaults.properties"

    // Configure which keys should be ignored by the plugin by providing regular expressions.
    // "sdk.dir" is ignored by default.
    ignoreList.add("keyToIgnore") // Ignore the key "keyToIgnore"
    ignoreList.add("sdk.*")       // Ignore all keys matching the regexp "sdk.*"
}
dependencies {
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation ("androidx.appcompat:appcompat:1.3.1")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation ("com.google.code.gson:gson:2.8.8")
    implementation ("androidx.compose.runtime:runtime:1.0.0")
    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-appcheck")
    implementation("com.google.firebase:firebase-dynamic-links-ktx:22.0.0")
    implementation("com.google.firebase:firebase-appcheck-playintegrity")
    implementation("com.google.firebase:firebase-database-ktx")

    implementation ("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
    implementation("com.google.api-client:google-api-client-android:1.23.0") {
        exclude(group = "org.apache.httpcomponents")
    }
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.android.libraries.places:places:3.3.0")

    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.9.0"))
    implementation ("com.google.api-client:google-api-client:2.0.0")
    implementation ("com.google.oauth-client:google-oauth-client-jetty:1.34.1")
    implementation ("com.google.apis:google-api-services-calendar:v3-rev20220715-2.0.0")
    implementation ("com.google.auth:google-auth-library-oauth2-http:0.23.0")
    implementation ("com.google.http-client:google-http-client-jackson2:1.11.0-beta")

    implementation ("pub.devrel:easypermissions:3.0.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation(libs.androidx.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.firebase.firestore)

    implementation ("androidx.viewpager2:viewpager2:1.0.0")

    implementation ("com.kakao.sdk:v2-all:2.20.0") // 전체 모듈 설치, 2.11.0 버전부터 지원
    implementation ("com.kakao.sdk:v2-user:2.20.0") // 카카오 로그인 API 모듈
    implementation ("com.kakao.sdk:v2-share:2.20.0") // 카카오톡 공유 API 모듈
    implementation ("com.kakao.sdk:v2-talk:2.20.0") // 카카오톡 채널, 카카오톡 소셜, 카카오톡 메시지 API 모듈
    implementation ("com.kakao.sdk:v2-friend:2.20.0") // 피커 API 모듈
    implementation ("com.kakao.sdk:v2-navi:2.20.0") // 카카오내비 API 모듈
    implementation ("com.kakao.sdk:v2-cert:2.20.0") // 카카오톡 인증 서비스 API 모듈

    implementation("com.google.android.gms:play-services-tasks:18.1.0")
    implementation ("com.google.android.gms:play-services-wearable:17.0.0")
    //implementation ("com.google.android.wearable:wearable:2.8.1")
    implementation("androidx.core:core-splashscreen:1.0.1")
}

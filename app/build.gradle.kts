plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.alarm__wars"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.alarm__wars"
        minSdk = 30
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    buildFeatures {
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    // 권한 설정 라이브러리
    implementation("com.google.android.gms:play-services-auth:19.0.0")
    implementation("com.naver.nid:naveridlogin-android-sdk:4.2.6")
    implementation("com.kakao.sdk:v2-user:2.5.0")

    implementation("com.karumi:dexter:6.2.1")

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
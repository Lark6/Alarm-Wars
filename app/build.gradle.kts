plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.alarm__wars"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.alarm__wars"
        minSdk = 31
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Firebase 서비스
    implementation(platform("com.google.firebase:firebase-bom:33.0.0")) // Firebase BOM을 사용하여 버전 관리
    implementation("com.google.firebase:firebase-analytics")            // Firebase 애널리틱스
    implementation("com.google.firebase:firebase-functions:19.0.2")    // Firebase 클라우드 함수
    implementation("com.google.firebase:firebase-auth")                 // Firebase 인증
    implementation("com.google.firebase:firebase-database-ktx")         // Firebase 실시간 데이터베이스 Kotlin 확장

    // Google Play 서비스
    implementation ("com.google.android.gms:play-services-auth:21.2.0") // Google 로그인 서비스

    // AndroidX 라이브러리
    implementation("androidx.appcompat:appcompat:1.6.1")               // 기본 Android 호환성 지원
    implementation("androidx.constraintlayout:constraintlayout:2.1.4") // 복잡한 레이아웃을 위한 제약 레이아웃
    implementation("androidx.navigation:navigation-fragment:2.5.3")    // 프래그먼트 기반 네비게이션을 처리하는 네비게이션 컴포넌트
    implementation("androidx.navigation:navigation-ui:2.5.3")          // 네비게이션 컴포넌트의 UI 요소

    // Material Design 컴포넌트
    implementation("com.google.android.material:material:1.11.0")      // Material Design 컴포넌트

    // 단위 테스트 라이브러리
    testImplementation("junit:junit:4.13.2")                           // 단위 테스트를 위한 JUnit

    // Android 테스트 라이브러리
    androidTestImplementation("androidx.test.ext:junit:1.1.5")         // JUnit4 통합을 위한 AndroidX Test Ext
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1") // UI 테스트를 위한 Espresso
}

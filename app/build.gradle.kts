plugins {
    alias(libs.plugins.android.application) // This already applies the Android application plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.projectfilm"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.projectfilm"
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
    buildFeatures {
        viewBinding = true
        dataBinding = true

    }
}

dependencies {

    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    implementation ("androidx.recyclerview:recyclerview:1.3.1")
    implementation("androidx.fragment:fragment:1.8.4")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation ("com.google.firebase:firebase-auth:22.3.0")


    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("com.google.android.material:material:1.12.0")
    implementation ("com.google.firebase:firebase-firestore:24.9.1")


    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation(libs.firebase.database)

    implementation("com.google.code.gson:gson:2.10.1") // Đã cập nhật phiên bản phổ biến
    implementation("com.squareup.okhttp3:okhttp:4.12.0") // Đã cập nhật phiên bản phổ biến
    implementation(files("libs/merchant-1.0.25.aar"))

    implementation ("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")

    val nav_version = "2.7.7"
    implementation ("androidx.navigation:navigation-fragment:$nav_version")
    implementation ("androidx.navigation:navigation-ui:$nav_version")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}

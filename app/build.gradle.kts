plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.stuntingdetection"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.stuntingdetection"
        minSdk = 24
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    //material design
    implementation("com.google.android.material:material:1.12.0")
    //rounded image view
    implementation("com.makeramen:roundedimageview:2.3.0")
    //navigation component
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    //bottomNaviagtion
    implementation( "com.google.android.material:material:1.12.0") // or the latest version
    implementation ("androidx.appcompat:appcompat:1.6.1") // or the latest version
    implementation ("androidx.core:core-ktx:1.12.0") // or the latest version

    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("com.google.android.material:material:1.9.0")

    //cards
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.material:material:1.2.1")

    //ml models

    implementation("com.google.firebase:firebase-ml-modeldownloader:24.1.0")
    implementation ("org.tensorflow:tensorflow-lite:2.12.0")

    //images
    implementation("com.github.bumptech.glide:glide:4.13.2")
    implementation("com.firebaseui:firebase-ui-storage:7.2.0")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")

    implementation("com.android.volley:volley:1.2.1")



}







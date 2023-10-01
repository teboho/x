plugins {
    id("com.android.application")
    // secrets
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")

    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")

}

android {
    namespace = "me.teboho.tweetjava"
    compileSdk = 34

    defaultConfig {
        applicationId = "me.teboho.tweetjava"
        minSdk = 30
        targetSdk = 33
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
        // enable buildConfig generation
        buildConfig = true
    }
}

dependencies {
    constraints {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.0") {
            because("kotlin-stdlib-jdk8 is now a part of kotlin-stdlib")
        }
    }

    // Import the Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
    // TODO: Add the dependencies for the Firebase products you want to use
    // When using the BOM, don't specify versions in Firebase dependecies
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")

    implementation ("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    // We want to use Hmac algo to encrypt text
    implementation ("commons-codec:commons-codec:1.15")

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.navigation:navigation-fragment:2.7.2")
    implementation("androidx.navigation:navigation-ui:2.7.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
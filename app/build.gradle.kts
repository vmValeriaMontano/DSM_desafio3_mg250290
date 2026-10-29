plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.aprendeapp"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.aprendeapp"
        minSdk = 24
        targetSdk = 36
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

    //View Binding para las vistas XML sin usar findViewById
    buildFeatures { viewBinding = true }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // consumo HTTP para consumir API REST y conversor JSON (Gson)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // para ver las peticiones y respuestas en el log (Logcat)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // biblioteca para cargar y mostrar imágenes desde internet
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // componentes visuales oficiales de Google
    implementation("com.google.android.material:material:1.12.0")

    // listas optimizadas y eficientes en pantalla
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // deslizar hacia abajo para actualizar la pantalla
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // ciclo de vida del componente
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    // hilos en segundo plano para tareas asíncronas (como peticiones de red)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

}
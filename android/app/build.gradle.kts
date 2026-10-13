import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Direccion del backend centralizada en un unico lugar (BuildConfig.API_BASE_URL).
// Prioridad: -Psmartbody.apiBaseUrl=... > smartbody.apiBaseUrl en local.properties > emulador.
// 10.0.2.2 es el localhost del computador visto desde el emulador de Android.
// Celular por USB: smartbody.apiBaseUrl=http://127.0.0.1:8080/ junto con "adb reverse tcp:8080 tcp:8080".
val propiedadesLocales = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
val apiBaseUrl: String = (project.findProperty("smartbody.apiBaseUrl") as String?)
    ?: propiedadesLocales.getProperty("smartbody.apiBaseUrl")
    ?: "http://10.0.2.2:8080/"

android {
    namespace = "com.smartbody"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.smartbody"
        minSdk = 26
        targetSdk = 36
        // El pipeline de lanzamiento (Jenkinsfile) fija la version con
        // -Psmartbody.versionName=X.Y.Z -Psmartbody.versionCode=<numero de build>.
        versionCode = (project.findProperty("smartbody.versionCode") as String?)?.toInt() ?: 1
        versionName = (project.findProperty("smartbody.versionName") as String?) ?: "1.0.0-SNAPSHOT"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.kotlinx.coroutines.android)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
}

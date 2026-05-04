plugins {
	id("com.android.application")
	id("org.jetbrains.kotlin.android")
	id("org.jetbrains.kotlin.kapt")
	id("org.jetbrains.kotlin.plugin.compose")
	id("com.google.gms.google-services")
	id("com.google.firebase.crashlytics")
}

android {
	namespace = "com.jigen.practicse"
	compileSdk = 35

	buildFeatures {
		compose = true
	}

	composeOptions {
		kotlinCompilerExtensionVersion = "1.5.14"
	}

	defaultConfig {
		applicationId = "com.jigen.practicse"
		minSdk = 24
		targetSdk = 35
		versionCode = 1
		versionName = "1.0"
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}

	kotlinOptions {
		jvmTarget = "17"
	}
}

dependencies {
	implementation("androidx.core:core-ktx:1.13.1")
	implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
	implementation("androidx.room:room-runtime:2.6.1")
	implementation("androidx.room:room-ktx:2.6.1")
	kapt("androidx.room:room-compiler:2.6.1")
	implementation("androidx.work:work-runtime-ktx:2.9.1")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
	implementation("com.google.code.gson:gson:2.11.0")
	implementation(platform("com.google.firebase:firebase-bom:33.2.0"))
	implementation("com.google.firebase:firebase-config-ktx")
	implementation("com.google.firebase:firebase-crashlytics-ktx")

	// Networking
	implementation("com.squareup.retrofit2:retrofit:2.9.0")
	implementation("com.squareup.retrofit2:converter-gson:2.9.0")
	implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
	implementation(platform("androidx.compose:compose-bom:2024.06.00"))
	implementation("androidx.activity:activity-compose:1.9.1")
	implementation("androidx.compose.foundation:foundation")
	implementation("androidx.compose.material3:material3")
	implementation("androidx.compose.runtime:runtime")
	implementation("androidx.compose.ui:ui")
}

// BuildConfig fields for Supabase; values should be supplied in gradle.properties or via CI
val supabaseUrl: String? = project.findProperty("SUPABASE_URL") as String?
val supabaseKey: String? = project.findProperty("SUPABASE_KEY") as String?

android {
	defaultConfig {
		buildConfigField("String", "SUPABASE_URL", "\"${supabaseUrl ?: ""}\"")
		buildConfigField("String", "SUPABASE_KEY", "\"${supabaseKey ?: ""}\"")
	}
}

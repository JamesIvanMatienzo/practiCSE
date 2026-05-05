plugins {
	id("com.android.application")
	id("org.jetbrains.kotlin.android")
	id("org.jetbrains.kotlin.kapt")
	id("org.jetbrains.kotlin.plugin.compose")
	id("com.google.gms.google-services")
	id("com.google.firebase.crashlytics")
}

import java.util.Properties

android {
	namespace = "com.jigen.practicse"
	compileSdk = 35

	buildFeatures {
		compose = true
		buildConfig = true
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

// Load Supabase configuration from local properties file (ignored by git)
val supabaseProperties = Properties().apply {
	val localSupabaseProperties = rootProject.file("supabase/supabase.properties")
	if (localSupabaseProperties.exists()) {
		localSupabaseProperties.inputStream().use { load(it) }
	}
}

fun resolveSupabaseValue(propertyName: String): String {
	fun normalize(raw: String): String {
		val value = raw.trim()
		return if (value.length >= 2 && value.first() == '"' && value.last() == '"') {
			value.substring(1, value.length - 1)
		} else {
			value
		}
	}

	val localValue = supabaseProperties.getProperty(propertyName)?.trim().orEmpty()
	if (localValue.isNotEmpty()) return normalize(localValue)
	val gradleValue = project.findProperty(propertyName) as String?
	return normalize(gradleValue?.trim().orEmpty())
}

val supabaseUrl = resolveSupabaseValue("SUPABASE_URL")
val supabaseKey = resolveSupabaseValue("SUPABASE_KEY")

// Apply BuildConfig fields for Supabase
android {
	defaultConfig {
		buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
		buildConfigField("String", "SUPABASE_KEY", "\"$supabaseKey\"")
	}
}

// Dependencies
dependencies {
	// Android Core
	implementation("androidx.core:core-ktx:1.13.1")
	implementation("androidx.appcompat:appcompat:1.7.0")
	implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
	implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")

	// Room Database
	implementation("androidx.room:room-runtime:2.6.1")
	implementation("androidx.room:room-ktx:2.6.1")
	kapt("androidx.room:room-compiler:2.6.1")

	// WorkManager
	implementation("androidx.work:work-runtime-ktx:2.9.1")

	// Coroutines
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

	// JSON
	implementation("com.google.code.gson:gson:2.11.0")

	// Firebase
	implementation(platform("com.google.firebase:firebase-bom:33.2.0"))
	implementation("com.google.firebase:firebase-config-ktx")
	implementation("com.google.firebase:firebase-crashlytics-ktx")

	// Networking (Retrofit + OkHttp)
	implementation("com.squareup.retrofit2:retrofit:2.9.0")
	implementation("com.squareup.retrofit2:converter-gson:2.9.0")
	implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

	// Jetpack Compose
	implementation(platform("androidx.compose:compose-bom:2024.06.00"))
	implementation("androidx.activity:activity-compose:1.9.1")
	implementation("androidx.compose.foundation:foundation")
	implementation("androidx.compose.material3:material3")
	implementation("androidx.compose.runtime:runtime")
	implementation("androidx.compose.ui:ui")
	implementation("androidx.compose.ui:ui-tooling-preview")
}

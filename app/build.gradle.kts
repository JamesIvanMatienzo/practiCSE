plugins {
	id("com.android.application")
	id("org.jetbrains.kotlin.android")
	id("org.jetbrains.kotlin.kapt")
}

android {
	namespace = "com.jigen.practicse"
	compileSdk = 35

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
}

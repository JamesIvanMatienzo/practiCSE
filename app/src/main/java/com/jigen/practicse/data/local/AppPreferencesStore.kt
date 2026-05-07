package com.jigen.practicse.data.local

import android.content.Context

private const val PREFS_NAME = "practicse_prefs"
private const val KEY_USER_NAME = "user_name"
private const val KEY_FIRST_NAME = "profile_first_name"
private const val KEY_MIDDLE_NAME = "profile_middle_name"
private const val KEY_SURNAME = "profile_surname"
private const val KEY_AGE = "profile_age"
private const val KEY_SCHOOL = "profile_school"
private const val KEY_PHOTO_URI = "profile_photo_uri"
private const val KEY_ACTIVE_TRACK = "active_track"
private const val KEY_OFFLINE_RANKING = "offline_ranking"

private const val TRACK_PROFESSIONAL = "professional"
private const val TRACK_SUB_PROFESSIONAL = "sub_professional"

data class UserProfileState(
	val firstName: String = "",
	val middleName: String = "",
	val surname: String = "",
	val age: String = "",
	val school: String = "",
	val photoUri: String? = null,
	val activeTrack: String = TRACK_PROFESSIONAL
) {
	val displayName: String
		get() = listOf(firstName, middleName, surname)
			.map { it.trim() }
			.filter { it.isNotBlank() }
			.joinToString(" ")
			.ifBlank { "You" }

	val activeTrackLabel: String
		get() = when (normalizeTrackKey(activeTrack)) {
			TRACK_SUB_PROFESSIONAL -> "Sub-Professional Track"
			else -> "Professional Track"
		}
}

class AppPreferencesStore(context: Context) {
	private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

	fun loadProfile(): UserProfileState {
		return UserProfileState(
			firstName = prefs.getString(KEY_FIRST_NAME, "").orEmpty(),
			middleName = prefs.getString(KEY_MIDDLE_NAME, "").orEmpty(),
			surname = prefs.getString(KEY_SURNAME, "").orEmpty(),
			age = prefs.getString(KEY_AGE, "").orEmpty(),
			school = prefs.getString(KEY_SCHOOL, "").orEmpty(),
			photoUri = prefs.getString(KEY_PHOTO_URI, null),
			activeTrack = getActiveTrackKey()
		)
	}

	fun saveProfile(profile: UserProfileState) {
		prefs.edit()
			.putString(KEY_FIRST_NAME, profile.firstName.trim())
			.putString(KEY_MIDDLE_NAME, profile.middleName.trim())
			.putString(KEY_SURNAME, profile.surname.trim())
			.putString(KEY_AGE, profile.age.trim())
			.putString(KEY_SCHOOL, profile.school.trim())
			.putString(KEY_PHOTO_URI, profile.photoUri?.trim())
			.putString(KEY_USER_NAME, profile.displayName)
			.putString(KEY_ACTIVE_TRACK, normalizeTrackKey(profile.activeTrack))
			.apply()
	}

	fun getDisplayName(): String {
		return prefs.getString(KEY_USER_NAME, null)
			?.trim()
			?.takeIf { it.isNotBlank() }
			?: loadProfile().displayName
	}

	fun getActiveTrackKey(): String {
		return normalizeTrackKey(prefs.getString(KEY_ACTIVE_TRACK, TRACK_PROFESSIONAL).orEmpty())
	}

	fun getActiveTrackLabel(): String = UserProfileState(activeTrack = getActiveTrackKey()).activeTrackLabel

	fun setActiveTrack(trackKey: String) {
		prefs.edit().putString(KEY_ACTIVE_TRACK, normalizeTrackKey(trackKey)).apply()
	}

	fun updateDisplayName(displayName: String) {
		prefs.edit().putString(KEY_USER_NAME, displayName.trim()).apply()
	}

	fun clearProfile() {
		prefs.edit()
			.remove(KEY_USER_NAME)
			.remove(KEY_FIRST_NAME)
			.remove(KEY_MIDDLE_NAME)
			.remove(KEY_SURNAME)
			.remove(KEY_AGE)
			.remove(KEY_SCHOOL)
			.remove(KEY_PHOTO_URI)
			.remove(KEY_ACTIVE_TRACK)
			.remove(KEY_OFFLINE_RANKING)
			.apply()
	}

	// Offline ranking preference (defaults to true)
	fun isOfflineRankingEnabled(): Boolean {
		return prefs.getBoolean(KEY_OFFLINE_RANKING, true)
	}

	fun setOfflineRankingEnabled(enabled: Boolean) {
		prefs.edit().putBoolean(KEY_OFFLINE_RANKING, enabled).commit()
	}
}

fun normalizeTrackKey(track: String?): String {
	return when (track?.trim()?.lowercase()) {
		TRACK_SUB_PROFESSIONAL,
		"sub-professional",
		"sub professional" -> TRACK_SUB_PROFESSIONAL
		else -> TRACK_PROFESSIONAL
	}
}

fun trackKeyToLabel(trackKey: String?): String {
	return when (normalizeTrackKey(trackKey)) {
		TRACK_SUB_PROFESSIONAL -> "Sub-Professional Track"
		else -> "Professional Track"
	}
}
package com.jigen.practicse.ui.screens.profile

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jigen.practicse.data.local.AppPreferencesStore
import com.jigen.practicse.data.local.PractiCSEDatabase
import com.jigen.practicse.data.local.UserProfileState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class ProfileViewModel(private val context: Context) : ViewModel() {
    private val store = AppPreferencesStore(context)
    private val db = PractiCSEDatabase.getInstance(context)

    private val _profileState = MutableStateFlow(store.loadProfile())
    val profileState: StateFlow<UserProfileState> = _profileState.asStateFlow()

    fun updateFirstName(name: String) {
        updateState { copy(firstName = name) }
    }

    fun updateMiddleName(name: String) {
        updateState { copy(middleName = name) }
    }

    fun updateSurname(name: String) {
        updateState { copy(surname = name) }
    }

    fun updateSchool(school: String) {
        updateState { copy(school = school) }
    }

    fun updatePhotoUri(uri: String?) {
        // If a valid URI is provided, copy it to app storage
        val savedPhotoPath = if (uri != null && uri.isNotBlank()) {
            try {
                val contentUri = Uri.parse(uri)
                context.contentResolver.openInputStream(contentUri)?.use { inputStream ->
                    val photoDir = File(context.cacheDir, "photos")
                    if (!photoDir.exists()) photoDir.mkdirs()
                    val photoFile = File(photoDir, "profile_photo.jpg")
                    photoFile.outputStream().use { output ->
                        inputStream.copyTo(output)
                    }
                    photoFile.absolutePath
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }

        updateState { copy(photoUri = savedPhotoPath) }
        // Immediately save the full profile when photo is updated
        saveProfile()
        // Also update leaderboard entry with the new photo
        updateLeaderboardPhoto()
    }

    private fun updateLeaderboardPhoto() {
        viewModelScope.launch {
            val profile = _profileState.value
            val photoBase64 = profile.photoUri?.let { photoPath ->
                try {
                    val file = File(photoPath)
                    if (file.exists()) {
                        val bytes = file.readBytes()
                        Base64.encodeToString(bytes, Base64.DEFAULT)
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }

            val displayName = profile.displayName
            
            try {
                val existingEntry = db.leaderboardDao().getForUser(displayName)
                if (existingEntry != null && photoBase64 != null) {
                    // Update existing entry with new photo
                    val updatedEntry = existingEntry.copy(photoBase64 = photoBase64)
                    db.leaderboardDao().upsert(updatedEntry)
                } else if (photoBase64 != null) {
                    // Create new entry if it doesn't exist and photo is available
                    db.leaderboardDao().upsert(
                        com.jigen.practicse.data.local.entity.LeaderboardEntryEntity(
                            userName = displayName,
                            totalScore = 0,
                            lastUpdatedMillis = System.currentTimeMillis(),
                            photoBase64 = photoBase64
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateState(update: UserProfileState.() -> UserProfileState) {
        val newState = _profileState.value.update()
        _profileState.value = newState
        // Instantly persist the combined username to local storage
        store.updateDisplayName(newState.displayName)
    }

    fun saveProfile() {
        store.saveProfile(_profileState.value)
        // Also update leaderboard with current photo
        updateLeaderboardPhoto()
    }

    fun logout() {
        store.clearProfile()
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProfileViewModel(context) as T
            }
        }
    }
}

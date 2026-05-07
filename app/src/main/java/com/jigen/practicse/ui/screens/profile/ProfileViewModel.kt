package com.jigen.practicse.ui.screens.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jigen.practicse.data.local.AppPreferencesStore
import com.jigen.practicse.data.local.UserProfileState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel(context: Context) : ViewModel() {
    private val store = AppPreferencesStore(context)

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

    fun updateAge(age: String) {
        updateState { copy(age = age) }
    }

    fun updatePhotoUri(uri: String?) {
        updateState { copy(photoUri = uri) }
    }

    private fun updateState(update: UserProfileState.() -> UserProfileState) {
        val newState = _profileState.value.update()
        _profileState.value = newState
        // Instantly persist the combined username to local storage
        store.updateDisplayName(newState.displayName)
    }

    fun saveProfile() {
        store.saveProfile(_profileState.value)
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

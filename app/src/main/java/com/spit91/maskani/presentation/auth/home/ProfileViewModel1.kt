package com.spit91.maskani.presentation.auth.home

import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.spit91.maskani.domain.model.repository.AuthRepository
import com.spit91.maskani.domain.model.repository.UserRepository

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    // internal mutable state(only this viewmodel can write updates here)
    private val _state = MutableStateFlow(ProfileState())
    // public read only state exposed to the UI screen
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    // Called by the screen each time it opens, so edits made on the Edit Profile page show up
    fun loadUserProfile() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            // read the signed-in user's document from users/{uid} in firestore
            userRepository.getUserProfile()
                .onSuccess { profile ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            user = profile,
                            errorMessage = if (profile == null) "Profile not found" else null
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.localizedMessage ?: "Could not load your profile"
                        )
                    }
                }
        }
    }
    // Action to open or close the confirmation popup
    fun setLogoutDialogVisible(visible: Boolean){
        _state.update { it.copy(showLogoutDialog = visible)}

    }
    //Action to execute final logout sequence
    fun confirmLogout(){
        viewModelScope.launch {
            // dismiss the popup dialog first
            _state.update {it.copy(showLogoutDialog = false, isLoading = true)}

            // end the firebase session so the user is really signed out
            authRepository.signOut()

            _state.update{it.copy(isLoading = false, isLoggedOut = true)}
        }
    }

    // Called with the image the user picked from their gallery
    fun onPhotoSelected(imageUri: Uri) {
        _state.update { it.copy(isUploadingPhoto = true, errorMessage = null) }

        viewModelScope.launch {
            userRepository.uploadProfilePhoto(imageUri)
                .onSuccess { url ->
                    _state.update {
                        it.copy(isUploadingPhoto = false, user = it.user?.copy(photoUrl = url))
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isUploadingPhoto = false,
                            errorMessage = exception.localizedMessage ?: "Could not upload your photo"
                        )
                    }
                }
        }
    }

    fun onEditProfileClick (){
        _state.update {currentState ->
            currentState.copy(isEditingProfile = true)
        }
    }

    fun resetEditProfileState(){
        _state.update {currentState ->
            currentState.copy(isEditingProfile = false)
        }
    }


}

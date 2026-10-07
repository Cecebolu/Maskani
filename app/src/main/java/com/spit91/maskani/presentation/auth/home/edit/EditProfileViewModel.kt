package com.spit91.maskani.presentation.auth.home.edit

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.spit91.maskani.domain.model.UserProfile
import com.spit91.maskani.domain.model.repository.UserRepository

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
): ViewModel() {
    //private control panel where the ViewModel edits screen details
    private val _state = MutableStateFlow(EditProfileState())
    val state = _state.asStateFlow()

    // the profile as it is stored in firestore, needed for its uid when saving
    private var loadedProfile: UserProfile? = null

    init{
        loadCurrentProfileData()
    }
    private fun loadCurrentProfileData () {
        //tell the state snapshot that we are loading data from the database
        _state.update{ it.copy(isLoading = true, errorMessage = null)}

        viewModelScope.launch {
            userRepository.getUserProfile()
                .onSuccess { profile ->
                    loadedProfile = profile
                    if (profile == null) {
                        _state.update { it.copy(isLoading = false, errorMessage = "Profile not found") }
                        return@onSuccess
                    }
                    //fallback to pre-fill the name field with the email if the name is missing
                    val preFilledName = profile.name.ifBlank { profile.email }

                    //push the final data snapshot to our stream window for the UI to display
                    _state.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            nameInput = preFilledName,
                            contactInput = profile.contact,
                            currentName = profile.name,
                            currentContact = profile.contact
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
    // trigger input every time the user types something
    fun onNameChange(newValue: String) {
        _state.update { it.copy(nameInput = newValue, errorMessage = null)}
    }
    fun onContactChange(newValue: String){
        _state.update {it.copy(contactInput = newValue, errorMessage = null)}
    }
    //save button
    fun SaveProfileChanges() {
        val profile = loadedProfile
        if (profile == null) {
            _state.update { it.copy(errorMessage = "Profile not loaded yet") }
            return
        }
        val newName = _state.value.nameInput.trim()
        val newContact = _state.value.contactInput.trim()
        if (newName.isBlank()) {
            _state.update { it.copy(errorMessage = "Name cannot be empty") }
            return
        }

        //freeze the save button and show the loader while firestore saves
        _state.update {it.copy(isSaving = true, errorMessage = null)}

        viewModelScope.launch {
            userRepository.updateUserProfile(profile.copy(name = newName, contact = newContact))
                .onSuccess {
                    //update state to confirm success so navigation routes can pop backwards
                    _state.update { currentState ->
                        currentState.copy(
                            isSaving = false,
                            isSavingSuccess = true
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = exception.localizedMessage ?: "Could not save your changes"
                        )
                    }
                }
        }
    }
    fun resetSaveState(){
        _state.update { currentState ->
            currentState.copy(
                isSavingSuccess = false,
                isSaving = false
            )
        }
    }
}

package com.spit91.maskani.presentation.auth.home

import com.spit91.maskani.domain.model.UserProfile
data class ProfileState (
    val isLoading: Boolean = false,
    val user: UserProfile? = null,
    val errorMessage: String? = null,
    val showLogoutDialog: Boolean = false,
    val isLoggedOut: Boolean = false,
    val isEditingProfile: Boolean = false,
    val isUploadingPhoto: Boolean = false
)

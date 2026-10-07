package com.spit91.maskani.presentation.auth.home.detail

import com.spit91.maskani.domain.model.Property

data class PropertyDetailState(
    val isLoading: Boolean = true,
    val property: Property? = null,
    val errorMessage: String? = null,

    // Request Viewing form
    val showRequestForm: Boolean = false,
    val selectedDateMillis: Long? = null,
    val selectedTime: String = "",
    val contact: String = "",
    val note: String = "",
    val isSubmitting: Boolean = false,
    val requestError: String? = null,
    val requestSent: Boolean = false
)

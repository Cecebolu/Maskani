package com.spit91.maskani.presentation.auth

data class SignUpState(
    val name: String = "",
    val email: String ="",
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)
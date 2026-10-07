package com.spit91.maskani.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {

    @Serializable data object AuthGraph : Screen
    @Serializable
    data object SignIn : Screen
    @Serializable
    data object SignUp : Screen
    @Serializable
    data object ForgotPassword : Screen

    @Serializable data object MainGraph : Screen

    @Serializable data object EditProfile : Screen

    // Nested Bottom tab graph destinations
    @Serializable
    data object Home : Screen
    @Serializable
    data class PropertyDetail(val propertyId: String) : Screen
    @Serializable data object Saved : Screen
    @Serializable data object MyRequests : Screen

    @Serializable data object Profile : Screen

}
package com.spit91.maskani.domain.model.repository

import android.net.Uri
import com.spit91.maskani.domain.model.UserProfile


interface UserRepository{
    suspend fun createUserProfile(profile: UserProfile): Result<Unit>
    suspend fun getUserProfile(): Result<UserProfile?>
    suspend fun updateUserProfile(profile: UserProfile): Result<Unit>
    suspend fun isUsernameTaken(username: String): Result<Boolean>
    // finds the email linked to a username so we can sign in with it; null if no such username
    suspend fun getEmailForUsername(username: String): Result<String?>
    // older accounts saved their username without an email; this adds it so username login works for them
    suspend fun linkEmailToUsername(username: String, email: String): Result<Unit>
    // uploads the picked image and saves its download url on the profile; returns that url
    suspend fun uploadProfilePhoto(imageUri: Uri): Result<String>

}
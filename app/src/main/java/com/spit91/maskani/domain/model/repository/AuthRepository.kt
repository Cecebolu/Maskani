package com.spit91.maskani.domain.model.repository

import com.spit91.maskani.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow
interface AuthRepository {
    val currentUser: Flow<AuthUser?>
    suspend fun signInWithEmail(email: String, password: String): kotlin.Result<AuthUser>
    suspend fun signUpWithEmail(email: String, password: String, name: String): kotlin.Result<AuthUser>
    suspend fun sendPasswordResetEmail(email: String): kotlin.Result<Unit>
    suspend fun signOut()
    suspend fun deleteCurrentUser(): kotlin.Result<Unit>
}
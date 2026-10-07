package com.spit91.maskani.domain.model.repository.usecase

import com.spit91.maskani.domain.model.AuthUser
import com.spit91.maskani.domain.model.UserProfile
import com.spit91.maskani.domain.model.repository.AuthRepository
import com.spit91.maskani.domain.model.repository.UserRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
){
    suspend operator fun invoke(
        email: String,
        password: String,
        confirmPassword: String,
        name: String,
        username: String
    ): Result<AuthUser> {
        val cleanUsername = username.trim().lowercase()
        if (email.isBlank() || password.isBlank() || name.isBlank() || cleanUsername.isBlank()) {
            return Result.failure(IllegalArgumentException("Name, username, email, and password cannot be empty"))
        }
        if (!cleanUsername.matches(Regex("^[a-z0-9_]{3,20}$"))) {
            return Result.failure(IllegalArgumentException("Username must be 3-20 characters: letters, numbers or _"))
        }
        if (password.length < 8) {
            return Result.failure(IllegalArgumentException("Password must be at least 8 characters"))
        }
        if (password != confirmPassword) {
            return Result.failure(IllegalArgumentException("Passwords do not match"))
        }
        //create the Auth account.If this fails, stop and pass the error up.
        val authUser: AuthUser = authRepository.signUpWithEmail(email, password, name).getOrElse {
            return Result.failure(it)
        }

        // check the username only now that we're signed in, so Firestore rules that require login allow the read.
        // if anything below fails, remove the new account so the user can try again with the same email.
        val taken = userRepository.isUsernameTaken(cleanUsername).getOrElse {
            authRepository.deleteCurrentUser()
            return Result.failure(Exception("Could not check username (step 2): ${it.localizedMessage}", it))
        }
        if (taken) {
            authRepository.deleteCurrentUser()
            return Result.failure(IllegalArgumentException("Username \"$cleanUsername\" is already taken"))
        }

        //save the matching profile document, keyed by the auth id.

        val profile = UserProfile(
            uid = authUser.id,
            name = name,
            username = cleanUsername,
            email = authUser.email
        )

        android.util.Log.d("SignUp", "saving profile for ${profile.uid}")
        val saveResult = userRepository.createUserProfile(profile)
        android.util.Log.d("SignUp", "save finished: $saveResult")
        saveResult.exceptionOrNull()?.let {
            authRepository.deleteCurrentUser()
            return Result.failure(Exception("Could not save profile (step 3): ${it.localizedMessage}", it))
        }
        return Result.success(authUser)

    }

}
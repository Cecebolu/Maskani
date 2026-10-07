package com.spit91.maskani.domain.model.repository.usecase
import javax.inject.Inject
import com.spit91.maskani.domain.model.AuthUser
import com.spit91.maskani.domain.model.UserProfile
import com.spit91.maskani.domain.model.repository.AuthRepository
import com.spit91.maskani.domain.model.repository.UserRepository

class SignInUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
){
    suspend operator fun invoke(username:String, password:String): Result<AuthUser> {
        val cleanUsername = username.trim().lowercase()
        if (cleanUsername.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Username and password cannot be empty"))
        }

        //Step 0: Firebase signs in with an email, so look up the email saved for this username.
        // an email typed in still works, so accounts created before username login can get in
        val email = if (cleanUsername.contains("@")) {
            cleanUsername
        } else {
            userRepository.getEmailForUsername(cleanUsername).getOrElse {
                return Result.failure(Exception("Could not check username: ${it.localizedMessage}", it))
            } ?: return Result.failure(IllegalArgumentException("No account found with that username"))
        }
        //Step 1: sign in. if this fails, pass the error straight back.
        val signInResult = authRepository.signInWithEmail(email, password)
        if (signInResult.isFailure) {
            return signInResult
        }
        val authUser = signInResult.getOrThrow()

        //Step 2: Safety net.only create a profile if the read SUCCEEDED and found nothing
        val profileResult = userRepository.getUserProfile()
        if (profileResult.isSuccess && profileResult.getOrNull() == null) {
            userRepository.createUserProfile(
                UserProfile(
                    uid = authUser.id,
                    name = authUser.displayName.orEmpty(),
                    email = authUser.email
                )
            )
        }
        // older accounts: save the email next to the username so next time they can log in with the username
        val profile = profileResult.getOrNull()
        if (profile != null && profile.username.isNotBlank() && cleanUsername.contains("@")) {
            userRepository.linkEmailToUsername(profile.username, authUser.email)
        }

        // sign in succeeds regardless of what happened with the profile
        return Result.success(authUser)
    }
}
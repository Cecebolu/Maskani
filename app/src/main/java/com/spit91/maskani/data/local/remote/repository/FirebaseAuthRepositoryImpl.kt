package com.spit91.maskani.data.repository

import android.R.attr.password
import com.google.firebase.auth.FirebaseAuth
import com.spit91.maskani.domain.model.AuthUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import  com.spit91.maskani.domain.model.repository.AuthRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class FirebaseAuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    // 1. REAL-TIME FLOW: Streams user login state shifts automatically
    override val currentUser: Flow<AuthUser?> = callbackFlow {
        //firebase calls this block automatically every time the auth state changes
        val listener = FirebaseAuth.AuthStateListener { auth ->
            // converts FirebaseUser object to your app's AuthUser data class
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                // checks if the user is signed in
                trySend(
                    AuthUser(
                        id = firebaseUser.uid,
                        email = firebaseUser.email.orEmpty(),
                        displayName = firebaseUser.displayName
                    )
                )
            } else {
                trySend(null)
            }
        }
        // registers the listener with the FirebaseAuth instance
        firebaseAuth.addAuthStateListener(listener)
        // removes the listener when the flow is closed so that the app don't leak memory or keep listening forever
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    // 2. SIGN IN: Converts Firebase's callback into a modern coroutine suspend function
    override suspend fun signInWithEmail(email: String, password: String): kotlin.Result<AuthUser> {
        return suspendCancellableCoroutine { continuation ->
            // trim(): phone keyboards often add a trailing space, which Firebase rejects as "badly formatted"
            firebaseAuth.signInWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener { result ->
                    val user = result.user
                    if (user != null) {
                        continuation.resume(
                            kotlin.Result.success(
                                AuthUser(
                                    id = user.uid,
                                    email = user.email.orEmpty(),
                                    displayName = user.displayName
                                )
                            )
                        )
                    } else {
                        continuation.resume(kotlin.Result.failure(Exception("User payload came back null.")))
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resume(kotlin.Result.failure(exception))
                }
        }
    }

    // 3. SIGN UP: Creates a new user profile on Firebase servers
   override suspend fun signUpWithEmail(email: String, password: String, name: String): kotlin.Result<AuthUser> {
       return try {
           val result = firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).await()
           val user = result.user ?: return kotlin.Result.failure(Exception("User generation failed."))
           //save the name on to the auth account itself.
           // if this fails, sign up still succeeds because the name is also saved in the firestore profile

           try {
               val changes = UserProfileChangeRequest.Builder()
                   .setDisplayName(name)
                   .build()
               user.updateProfile(changes).await()
           } catch (e: CancellationException) {
               throw e
           } catch (e: Exception) {
               // ignore
           }
           kotlin.Result.success(
               AuthUser(
                   id = user.uid,
                   email = user.email.orEmpty(),
                   displayName = name
               )
           )
       } catch (e: CancellationException) {
           throw e
       }catch (e: Exception) {
           kotlin.Result.failure(e)
       }
   }

    // 5. FORGOT PASSWORD: Sends a reset link to the user's email via Firebase
    override suspend fun sendPasswordResetEmail(email: String): kotlin.Result<Unit> {
        return suspendCancellableCoroutine { continuation ->
            firebaseAuth.sendPasswordResetEmail(email.trim())
                .addOnSuccessListener {
                    continuation.resume(kotlin.Result.success(Unit))
                }
                .addOnFailureListener { exception ->
                    continuation.resume(kotlin.Result.failure(exception))
                }
        }
    }

    // 4. SIGN OUT: Completely tears down the active session tokens
    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    // 6. DELETE: Removes a just-created account when sign up can't be completed (e.g. username taken)
    override suspend fun deleteCurrentUser(): kotlin.Result<Unit> {
        return try {
            firebaseAuth.currentUser?.delete()?.await()
            kotlin.Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            kotlin.Result.failure(e)
        }
    }
}
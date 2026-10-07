package com.spit91.maskani.data.local.remote.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.spit91.maskani.domain.model.UserProfile
import com.spit91.maskani.domain.model.repository.UserRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FirestoreUserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val storage: FirebaseStorage
) : UserRepository {
    private val usersCollection = firestore.collection("users")
    // usernames/{username} -> { uid }. Keeps usernames unique without letting anyone read other users' profiles
    private val usernamesCollection = firestore.collection("usernames")

    override suspend fun createUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            val batch = firestore.batch()
            batch.set(usersCollection.document(profile.uid), profile)
            if (profile.username.isNotBlank()) {
                batch.set(
                    usernamesCollection.document(profile.username),
                    mapOf("uid" to profile.uid, "email" to profile.email)
                )
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserProfile() : Result<UserProfile?> {
        return try {
            val uid = firebaseAuth.currentUser?.uid
                ?: return Result.failure(Exception("No signed-in user."))
            val snapshot = usersCollection.document(uid).get().await()
            val profile = snapshot.toObject(UserProfile::class.java)
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            usersCollection.document(profile.uid)
                .update(mapOf("name" to  profile.name, "contact" to profile.contact))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isUsernameTaken(username: String): Result<Boolean> {
        return try {
            Result.success(usernamesCollection.document(username).get().await().exists())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getEmailForUsername(username: String): Result<String?> {
        return try {
            val snapshot = usernamesCollection.document(username).get().await()
            Result.success(snapshot.getString("email"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun linkEmailToUsername(username: String, email: String): Result<Unit> {
        return try {
            usernamesCollection.document(username).update("email", email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadProfilePhoto(imageUri: Uri): Result<String> {
        return try {
            val uid = firebaseAuth.currentUser?.uid
                ?: return Result.failure(Exception("No signed-in user."))
            // one photo per user at profile_photos/{uid}.jpg, so a new upload replaces the old one
            val photoRef = storage.reference.child("profile_photos/$uid.jpg")
            photoRef.putFile(imageUri).await()
            val downloadUrl = photoRef.downloadUrl.await().toString()
            usersCollection.document(uid).update("photoUrl", downloadUrl).await()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}

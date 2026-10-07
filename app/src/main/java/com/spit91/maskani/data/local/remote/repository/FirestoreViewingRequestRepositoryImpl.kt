package com.spit91.maskani.data.local.remote.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.spit91.maskani.domain.model.ViewingRequest
import com.spit91.maskani.domain.model.ViewingStatus
import com.spit91.maskani.domain.model.repository.ViewingRequestRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class FirestoreViewingRequestRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : ViewingRequestRepository {
    private val requestsCollection = firestore.collection("viewingRequests")

    override suspend fun createRequest(request: ViewingRequest): Result<Unit> {
        return try {
            val uid = firebaseAuth.currentUser?.uid
                ?: return Result.failure(Exception("No signed-in user."))
            val document = requestsCollection.document()
            document.set(
                request.copy(
                    id = document.id,
                    buyerId = uid,
                    status = ViewingStatus.PENDING,
                    createdAt = System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMyRequests(): Result<List<ViewingRequest>> {
        return try {
            val uid = firebaseAuth.currentUser?.uid
                ?: return Result.failure(Exception("No signed-in user."))
            val snapshot = requestsCollection
                .whereEqualTo("buyerId", uid)
                .get()
                .await()
            // sorted here instead of with orderBy() so Firestore doesn't need a composite index
            val requests = snapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(ViewingRequest::class.java)?.copy(id = document.id)
                } catch (e: RuntimeException) {
                    android.util.Log.e("ViewingRequestRepo", "Skipping bad request ${document.id}: ${e.message}")
                    null
                }
            }.sortedByDescending { it.createdAt }
            Result.success(requests)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelRequest(requestId: String): Result<Unit> {
        return try {
            requestsCollection.document(requestId)
                .update("status", ViewingStatus.CANCELLED)
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

package com.spit91.maskani.data.local.remote.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.spit91.maskani.domain.model.Property
import com.spit91.maskani.domain.model.PropertyStatus
import com.spit91.maskani.domain.model.repository.PropertyRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class FirestorePropertyRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
): PropertyRepository{
    private val propertiesCollection = firestore.collection("properties")

    override suspend fun getPublishedProperties(): Result<List<Property>> {
        return try{
            val snapshot = propertiesCollection
                .whereEqualTo("status", PropertyStatus.PUBLISHED)
                .get()
                .await()

            // skip documents with wrong field types (e.g. price saved as text) instead of failing the whole list
            val properties = snapshot.documents.mapNotNull{document ->
                try {
                    document.toObject(Property::class.java)?.copy(id = document.id)
                } catch (e: RuntimeException) {
                    android.util.Log.e("PropertyRepo", "Skipping bad property ${document.id}: ${e.message}")
                    null
                }
            }
            Result.success(properties)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPropertyById(propertyId: String) : Result<Property?> {
        return try {
            val document = propertiesCollection.document(propertyId).get().await()
            val property = document.toObject(Property::class.java)
                ?.copy(id = document.id)
                ?.takeIf{it.status == PropertyStatus.PUBLISHED}
            Result.success(property)
        } catch (e: CancellationException) {
            throw e
        }catch (e: Exception) {
            Result.failure(e)
        }
    }
}
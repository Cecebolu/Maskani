package com.spit91.maskani.di

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.spit91.maskani.data.local.remote.repository.FirestorePropertyRepositoryImpl
import com.spit91.maskani.data.local.remote.repository.FirestoreSavedPropertyRepositoryImpl
import com.spit91.maskani.data.local.remote.repository.FirestoreUserRepositoryImpl
import com.spit91.maskani.data.local.remote.repository.FirestoreViewingRequestRepositoryImpl
import com.spit91.maskani.domain.model.repository.ViewingRequestRepository
import com.spit91.maskani.domain.model.repository.PropertyRepository
import com.spit91.maskani.domain.model.repository.SavedPropertyRepository
import com.spit91.maskani.domain.model.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FirestoreModule{

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: FirestoreUserRepositoryImpl
    ) : UserRepository

    @Binds
    @Singleton
    abstract fun bindPropertyRepository(
        propertyRepositoryImpl: FirestorePropertyRepositoryImpl
    ): PropertyRepository
    @Binds
    @Singleton
    abstract fun bindSavePropertyRepository(
        savedPropertyRepositoryImpl: FirestoreSavedPropertyRepositoryImpl
    ): SavedPropertyRepository

    @Binds
    @Singleton
    abstract fun bindViewingRequestRepository(
        viewingRequestRepositoryImpl: FirestoreViewingRequestRepositoryImpl
    ): ViewingRequestRepository

    companion object {
        //Tells hilt how to provide firestore: calls FirebaseFirestore.getInstance()
        @Provides
        @Singleton
        fun provideFirestore(): FirebaseFirestore {
            return FirebaseFirestore.getInstance()
        }

        //Tells hilt how to provide firebase storage (used for profile photos)
        @Provides
        @Singleton
        fun provideFirebaseStorage(): FirebaseStorage {
            return FirebaseStorage.getInstance()
        }
    }

}
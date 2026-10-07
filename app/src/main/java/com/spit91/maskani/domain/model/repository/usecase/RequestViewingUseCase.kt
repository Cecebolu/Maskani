package com.spit91.maskani.domain.model.repository.usecase

import com.spit91.maskani.domain.model.Property
import com.spit91.maskani.domain.model.ViewingRequest
import com.spit91.maskani.domain.model.ViewingStatus
import com.spit91.maskani.domain.model.repository.UserRepository
import com.spit91.maskani.domain.model.repository.ViewingRequestRepository
import javax.inject.Inject

class RequestViewingUseCase @Inject constructor(
    private val viewingRequestRepository: ViewingRequestRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        property: Property,
        preferredDate: String,
        preferredTime: String,
        contact: String,
        note: String
    ): Result<Unit> {
        if (preferredDate.isBlank() || preferredTime.isBlank()) {
            return Result.failure(IllegalArgumentException("Please choose a date and time"))
        }
        val cleanContact = contact.trim()
        if (cleanContact.count { it.isDigit() } < 9) {
            return Result.failure(IllegalArgumentException("Please enter a valid phone number so Maskani can confirm"))
        }

        // one open request per property is enough; stops buyers spamming the same listing
        val existing = viewingRequestRepository.getMyRequests().getOrElse { return Result.failure(it) }
        if (existing.any { it.propertyId == property.id && it.status == ViewingStatus.PENDING }) {
            return Result.failure(IllegalStateException("You already have a pending request for this property"))
        }

        val profile = userRepository.getUserProfile().getOrNull()
        return viewingRequestRepository.createRequest(
            ViewingRequest(
                propertyId = property.id,
                propertyTitle = property.title,
                propertyLocation = property.location,
                buyerName = profile?.name.orEmpty(),
                buyerEmail = profile?.email.orEmpty(),
                buyerContact = cleanContact,
                preferredDate = preferredDate,
                preferredTime = preferredTime,
                note = note.trim()
            )
        )
    }
}

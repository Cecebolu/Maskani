package com.spit91.maskani.domain.model.repository.usecase

import com.spit91.maskani.domain.model.repository.ViewingRequestRepository
import javax.inject.Inject

class CancelViewingRequestUseCase @Inject constructor(
    private val viewingRequestRepository: ViewingRequestRepository
) {
    suspend operator fun invoke(requestId: String): Result<Unit> {
        if (requestId.isBlank()) {
            return Result.failure(IllegalArgumentException("Request could not be identified"))
        }
        return viewingRequestRepository.cancelRequest(requestId)
    }
}

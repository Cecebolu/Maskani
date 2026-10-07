package com.spit91.maskani.domain.model.repository.usecase

import com.spit91.maskani.domain.model.ViewingRequest
import com.spit91.maskani.domain.model.repository.ViewingRequestRepository
import javax.inject.Inject

class GetMyViewingRequestsUseCase @Inject constructor(
    private val viewingRequestRepository: ViewingRequestRepository
) {
    suspend operator fun invoke(): Result<List<ViewingRequest>> = viewingRequestRepository.getMyRequests()
}

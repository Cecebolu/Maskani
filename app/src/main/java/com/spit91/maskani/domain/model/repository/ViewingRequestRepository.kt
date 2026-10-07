package com.spit91.maskani.domain.model.repository

import com.spit91.maskani.domain.model.ViewingRequest

interface ViewingRequestRepository {
    suspend fun createRequest(request: ViewingRequest): Result<Unit>
    suspend fun getMyRequests(): Result<List<ViewingRequest>>
    suspend fun cancelRequest(requestId: String): Result<Unit>
}

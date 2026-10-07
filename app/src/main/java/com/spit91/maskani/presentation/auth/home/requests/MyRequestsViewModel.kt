package com.spit91.maskani.presentation.auth.home.requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spit91.maskani.domain.model.ViewingRequest
import com.spit91.maskani.domain.model.repository.usecase.CancelViewingRequestUseCase
import com.spit91.maskani.domain.model.repository.usecase.GetMyViewingRequestsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyRequestsState(
    val isLoading: Boolean = false,
    val requests: List<ViewingRequest> = emptyList(),
    val errorMessage: String? = null,
    val requestToCancel: ViewingRequest? = null,
    val transientMessage: String? = null
)

@HiltViewModel
class MyRequestsViewModel @Inject constructor(
    private val getMyViewingRequestsUseCase: GetMyViewingRequestsUseCase,
    private val cancelViewingRequestUseCase: CancelViewingRequestUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MyRequestsState())
    val state: StateFlow<MyRequestsState> = _state.asStateFlow()

    // called each time the screen opens, so status changes made by Maskani staff show up
    fun loadRequests() {
        _state.update { it.copy(isLoading = it.requests.isEmpty(), errorMessage = null) }
        viewModelScope.launch {
            getMyViewingRequestsUseCase()
                .onSuccess { requests ->
                    _state.update { it.copy(isLoading = false, requests = requests) }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(isLoading = false, errorMessage = exception.localizedMessage ?: "Could not load your requests")
                    }
                }
        }
    }

    fun askToCancel(request: ViewingRequest?) {
        _state.update { it.copy(requestToCancel = request) }
    }

    fun confirmCancel() {
        val request = _state.value.requestToCancel ?: return
        _state.update { it.copy(requestToCancel = null) }
        viewModelScope.launch {
            cancelViewingRequestUseCase(request.id)
                .onSuccess {
                    _state.update { it.copy(transientMessage = "Request cancelled") }
                    loadRequests()
                }
                .onFailure { exception ->
                    _state.update { it.copy(transientMessage = exception.localizedMessage ?: "Could not cancel request") }
                }
        }
    }

    fun onMessageShown() {
        _state.update { it.copy(transientMessage = null) }
    }
}

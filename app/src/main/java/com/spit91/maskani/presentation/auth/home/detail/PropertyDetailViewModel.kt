package com.spit91.maskani.presentation.auth.home.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.spit91.maskani.domain.model.repository.UserRepository
import com.spit91.maskani.domain.model.repository.usecase.GetPropertyUseCase
import com.spit91.maskani.domain.model.repository.usecase.RequestViewingUseCase
import com.spit91.maskani.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class PropertyDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPropertyUseCase: GetPropertyUseCase,
    private val requestViewingUseCase: RequestViewingUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    private val propertyId = savedStateHandle.toRoute<Screen.PropertyDetail>().propertyId

    private val _state = MutableStateFlow(PropertyDetailState())
    val state: StateFlow<PropertyDetailState> = _state.asStateFlow()

    init {
        loadProperty()
        prefillContact()
    }

    fun loadProperty() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            getPropertyUseCase(propertyId)
                .onSuccess { property ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            property = property,
                            errorMessage = if (property == null) "This property is no longer available" else null
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(isLoading = false, errorMessage = exception.localizedMessage ?: "Could not load property")
                    }
                }
        }
    }

    // fill in the buyer's own number from their profile so they don't have to type it every time
    private fun prefillContact() {
        viewModelScope.launch {
            val contact = userRepository.getUserProfile().getOrNull()?.contact.orEmpty()
            _state.update { if (it.contact.isBlank()) it.copy(contact = contact) else it }
        }
    }

    fun openRequestForm() {
        _state.update { it.copy(showRequestForm = true, requestError = null) }
    }

    fun closeRequestForm() {
        _state.update { it.copy(showRequestForm = false, requestError = null) }
    }

    fun onDateSelected(millis: Long?) {
        _state.update { it.copy(selectedDateMillis = millis, requestError = null) }
    }

    fun onTimeSelected(time: String) {
        _state.update { it.copy(selectedTime = time, requestError = null) }
    }

    fun onContactChange(value: String) {
        _state.update { it.copy(contact = value, requestError = null) }
    }

    fun onNoteChange(value: String) {
        _state.update { it.copy(note = value, requestError = null) }
    }

    fun submitRequest() {
        val current = _state.value
        val property = current.property ?: return
        val date = current.selectedDateMillis?.let { formatStorageDate(it) }.orEmpty()

        _state.update { it.copy(isSubmitting = true, requestError = null) }
        viewModelScope.launch {
            requestViewingUseCase(
                property = property,
                preferredDate = date,
                preferredTime = current.selectedTime,
                contact = current.contact,
                note = current.note
            )
                .onSuccess {
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            showRequestForm = false,
                            requestSent = true,
                            selectedDateMillis = null,
                            selectedTime = "",
                            note = ""
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(isSubmitting = false, requestError = exception.localizedMessage ?: "Could not send request")
                    }
                }
        }
    }

    fun onRequestSentShown() {
        _state.update { it.copy(requestSent = false) }
    }

    // DatePicker gives midnight UTC, so format in UTC to avoid the date shifting by a day
    private fun formatStorageDate(millis: Long): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(millis)
}

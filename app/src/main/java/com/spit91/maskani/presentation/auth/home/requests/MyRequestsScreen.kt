package com.spit91.maskani.presentation.auth.home.requests

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spit91.maskani.domain.model.ViewingRequest
import com.spit91.maskani.domain.model.ViewingStatus
import com.spit91.maskani.presentation.auth.home.detail.formatViewingDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRequestsScreen(
    onPropertyClick: (String) -> Unit,
    viewModel: MyRequestsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val errorMessage = state.errorMessage
    val snackbarHostState = remember { SnackbarHostState() }

    LifecycleResumeEffect(Unit) {
        viewModel.loadRequests()
        onPauseOrDispose { }
    }
    LaunchedEffect(state.transientMessage) {
        val message = state.transientMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.onMessageShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopAppBar(title = { Text("My Viewings", fontWeight = FontWeight.Bold) }) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.isLoading -> CircularProgressIndicator()
                errorMessage != null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(errorMessage, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { viewModel.loadRequests() }) { Text("Try again") }
                    }
                }
                state.requests.isEmpty() -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text("No viewing requests yet", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Open a property and tap Request Viewing to arrange a visit.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.requests, key = { it.id }) { request ->
                            ViewingRequestCard(
                                request = request,
                                onClick = { onPropertyClick(request.propertyId) },
                                onCancel = { viewModel.askToCancel(request) }
                            )
                        }
                    }
                }
            }
        }
    }

    val requestToCancel = state.requestToCancel
    if (requestToCancel != null) {
        AlertDialog(
            onDismissRequest = { viewModel.askToCancel(null) },
            title = { Text("Cancel this viewing?") },
            text = { Text("Your request for ${requestToCancel.propertyTitle} will be cancelled.") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmCancel() }) { Text("Cancel viewing") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.askToCancel(null) }) { Text("Keep it") }
            }
        )
    }
}

@Composable
private fun ViewingRequestCard(
    request: ViewingRequest,
    onClick: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = request.propertyTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(request.status)
            }
            Text(
                text = request.propertyLocation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${formatViewingDate(request.preferredDate)} · ${request.preferredTime}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = statusHint(request.status),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (request.status == ViewingStatus.PENDING) {
                TextButton(onClick = onCancel, modifier = Modifier.align(Alignment.End)) {
                    Text("Cancel request", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (label, color) = when (status) {
        ViewingStatus.PENDING -> "Pending" to Color(0xFFB26A00)
        ViewingStatus.CONFIRMED -> "Confirmed" to Color(0xFF1B7F3B)
        ViewingStatus.COMPLETED -> "Done" to Color(0xFF3D5AFE)
        ViewingStatus.CANCELLED -> "Cancelled" to Color(0xFF757575)
        else -> status to Color(0xFF757575)
    }
    Text(
        text = label,
        color = color,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

private fun statusHint(status: String): String = when (status) {
    ViewingStatus.PENDING -> "Maskani is arranging this with the landlord"
    ViewingStatus.CONFIRMED -> "Confirmed. Maskani will share meeting details with you"
    ViewingStatus.COMPLETED -> "Viewing done"
    ViewingStatus.CANCELLED -> "This request was cancelled"
    else -> ""
}

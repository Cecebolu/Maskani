package com.spit91.maskani.presentation.auth.home

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spit91.maskani.domain.model.UnitType
import androidx.compose.runtime.setValue



private val unitTypeFilters = listOf(
    UnitType.APARTMENT,
    UnitType.HOUSE,
    UnitType.VILLA,
    UnitType.TOWN_HOUSE,
    UnitType.BEDSITTER,
    UnitType.STUDIO,
    UnitType.ONE_BEDROOM,
    UnitType.TWO_BEDROOM,
    UnitType.THREE_BEDROOM,
    UnitType.FOUR_PLUS_BEDROOM
)
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onPropertyClick: (String) -> Unit = {}
){
    val state by viewModel.state.collectAsStateWithLifecycle()
    val errorMessage = state.errorMessage
    val visibleProperties = state.visibleProperties

    val snackbarHostState = remember { SnackbarHostState() }

    // refresh which listings are saved whenever this screen comes back into view
    LifecycleResumeEffect(Unit) {
        viewModel.loadSavedIds()
        onPauseOrDispose { }
    }
    // Show an error message at the bottom when saving fails
    LaunchedEffect(state.transientMessage) {
        val message = state.transientMessage
        if(message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.onMessageShown()
        }
    }

    Scaffold(
        snackbarHost = {SnackbarHost (snackbarHostState)},
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Maskani",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                )
            )
        }
    ){ paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ){
            Text(
                text = "Find your dream home",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            //search bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it)},
                placeholder = { Text("Search by title or location...")},
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search")},
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()){
                        IconButton(onClick = { viewModel.onSearchQueryChange("")}) {
                            Icon(Icons.Default.Close, contentDescription = "Clear Search")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ){
                item {
                    FilterChip(
                        selected = state.selectedUnitType == null,
                        onClick = { viewModel.onUnitTypeSelected(null)},
                        label = {Text("All")}
                    )
                }
                items(unitTypeFilters) { unitType ->
                    FilterChip(
                        selected = state.selectedUnitType == unitType,
                        onClick = { viewModel.onUnitTypeSelected(unitType)},
                        label = { Text(unitTypeLabel(unitType))}
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // location and price filters as dropdowns

            Row(horizontalArrangement = Arrangement.spacedBy (8.dp)) {

                FilterDropDown(
                    label = "Location",
                    selectedLabel = state.selectedLocation,
                    allLabel = "All locations",
                    options = state.availableLocations,
                    optionLabel = { it },
                    onSelect = { viewModel.onLocationSelected(it)}
                )
                FilterDropDown(
                    label = "Price",
                    selectedLabel = state.selectedPriceRange?.label,
                    allLabel = "All prices",
                    options = PriceRange.entries,
                    optionLabel = { it.label },
                    onSelect = { viewModel.onPriceRangeSelected(it)}

                )

            }

            // Listings
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ){
                when {
                    state.isLoading ->{
                        CircularProgressIndicator()
                    }
                    errorMessage != null -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = {viewModel.loadProperties()}) {
                                Text("Try again")
                            }
                        }
                    }
                    state.properties.isEmpty() -> {
                        Text(
                            text = "No properties available yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    visibleProperties.isEmpty() -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No properties match your search",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(onClick = { viewModel.clearFilters()}) {
                                Text("Clear filters")
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ){
                            items(visibleProperties, key = {it.id }) { property ->
                                PropertyCard(
                                    property = property,
                                    onClick = { onPropertyClick(property.id)},
                                    isSaved = property.id in state.savedIds,
                                    onToggleSaved = { viewModel.toggleSaved(property.id)}
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> FilterDropDown (
    label: String,
    selectedLabel: String?,
    allLabel: String,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T?) -> Unit
) {
    var expanded by remember { mutableStateOf(false)}

    Box {
        FilterChip(
            selected = selectedLabel != null,
            onClick = { expanded = true },
            label = { Text(selectedLabel ?: label) },
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)

            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false}
        ){
            DropdownMenuItem(
                text = { Text(allLabel) },
                onClick = {
                    onSelect(null)
                    expanded = false
                }
            )
            options.forEach{option ->
                DropdownMenuItem(
                    text = {Text(optionLabel(option))},
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }

}
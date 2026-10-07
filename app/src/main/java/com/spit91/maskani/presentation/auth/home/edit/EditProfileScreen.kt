package com.spit91.maskani.presentation.auth.home.edit

import androidx.compose.runtime.Composable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Card
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit, // callback to navigate back to the previous screen
    modifier: Modifier = Modifier,
    viewModel: EditProfileViewModel = hiltViewModel<EditProfileViewModel>() // dependency injection: link the edit profile screen to the edit profile viewmodel

){
    // collect the state stream from the viewmodel
    val state by viewModel.state.collectAsStateWithLifecycle()

    //close the page automatically the moment isSavingSucces is true
    LaunchedEffect(state.isSavingSuccess) {
        if (state.isSavingSuccess) {
            onNavigateBack()
            //clear the save success flag
            viewModel.resetSaveState()
        }
    }
    Scaffold(
        topBar = {
            androidx.compose.material3.CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Edit Profile",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 50.dp, top = 8.dp),
                color = Color.Transparent // Transparent background
            ) {
                Button(
                    onClick = {viewModel.SaveProfileChanges()},
                    enabled = !state.isSaving && !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EE),
                        contentColor = Color.Black
                    )
                ){
                    if(state.isSaving){
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.LightGray,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Save Changes",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    ){paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                //automatically grab the first 2 characters of the user's name and make them uppercase
                val initials = state.nameInput.trim().split("")
                    .filter{it.isNotEmpty()}
                    .take(2)
                    .joinToString("") {it.take(1)}
                    .uppercase()
                Text(
                    text = initials,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Personal Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Update your personal Information",
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(24.dp))

            state.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 4.dp),
                shape = RoundedCornerShape(16.dp),

            ){
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                  Row(
                      modifier = Modifier.fillMaxWidth(),
                      verticalAlignment = Alignment.Top
                  ){
                      Icon(
                          imageVector = Icons.Default.Person,
                          contentDescription = null,
                          tint = MaterialTheme.colorScheme.primary,
                          modifier = Modifier.padding(top = 2.dp,end = 12.dp)
                      )
                      Column(modifier = Modifier.weight(1f)){
                          Text(
                              text = "Full Name",
                              fontWeight = FontWeight.Bold,
                              style = MaterialTheme.typography.bodyMedium
                          )
                          Text(
                              text = "Your public account name",
                              style = MaterialTheme.typography.bodySmall,
                              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha= 0.4f)
                          )
                          Spacer(modifier = Modifier.height(8.dp))
                          // input container with rounded corners
                          OutlinedTextField(
                              value = state.nameInput,
                              onValueChange = { viewModel.onNameChange(it)},
                              modifier = Modifier.fillMaxWidth(),
                              singleLine = true,
                              shape = RoundedCornerShape(24.dp),
                              colors = OutlinedTextFieldDefaults.colors(
                                  focusedBorderColor = MaterialTheme.colorScheme.primary,
                                  unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                              )
                          )
                      }
                  }
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ){
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 2.dp, end = 12.dp)
                        )
                        Column(modifier = Modifier.weight(1f)){
                            Text(
                                text = "Contact",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Update your contact details",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = state.contactInput,
                                onValueChange = { viewModel.onContactChange(it)},
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(24.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }
                }
            }

        }
    }
}
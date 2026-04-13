package com.example.wnhu_android_app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    app: AppVariables,
    userData: UserData,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    var firstName by remember { mutableStateOf(userData.user.firstName) }
    var lastName by remember { mutableStateOf(userData.user.lastName) }
    var isUNHStudent by remember { mutableStateOf(false) }
    var age by remember { mutableIntStateOf(18) }
    var gender by remember { mutableStateOf("Rather not say") }
    var ageExpanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }

    val ages = (13..100).toList()
    val genders = listOf("Male", "Female", "Rather not say")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Setup Your WNHU Profile", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("UNH Student", modifier = Modifier.weight(1f))
            Switch(
                checked = isUNHStudent,
                onCheckedChange = { isUNHStudent = it }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = genderExpanded,
            onExpandedChange = { genderExpanded = it }
        ) {
            OutlinedTextField(
                value = gender,
                onValueChange = {},
                readOnly = true,
                label = { Text("Gender") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            DropdownMenu(
                expanded = genderExpanded,
                onDismissRequest = { genderExpanded = false }
            ) {
                genders.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            gender = option
                            genderExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = ageExpanded,
            onExpandedChange = { ageExpanded = it }
        ) {
            OutlinedTextField(
                value = age.toString(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Age") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ageExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            DropdownMenu(
                expanded = ageExpanded,
                onDismissRequest = { ageExpanded = false }
            ) {
                ages.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.toString()) },
                        onClick = {
                            age = option
                            ageExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                authViewModel.createUser(
                    context = context,
                    app = app,
                    userData = userData,
                    firstName = firstName,
                    lastName = lastName,
                    age = age,
                    gender = gender,
                    isUNHStudent = isUNHStudent
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = firstName.isNotBlank() && lastName.isNotBlank() && !authViewModel.isWorking
        ) {
            Text("Create Account")
        }

        if (authViewModel.authError != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = authViewModel.authError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (userData.user.email.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Signed in as ${userData.user.email}", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

package com.example.wnhu_android_app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    userData: UserData,
    app: AppVariables,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }
    val displayName = userData.user.fullName.ifBlank { "Guest Listener" }
    val displayEmail = userData.user.email.ifBlank {
        if (app.isGuest) "Signed in as guest" else "Not signed in"
    }

    LaunchedEffect(showSheet) {
        if (showSheet) {
            userData.refreshSongRatings()
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            LikedSongsSheet(userData)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier
                .size(140.dp)
                .padding(top = 16.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = displayName,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp)
        )

        Text(
            text = displayEmail,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        // Liked Songs Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showSheet = true }
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ThumbUp, contentDescription = null)
                Spacer(Modifier.width(16.dp))
                Text("Liked Songs", style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(Modifier.height(24.dp))

        // Logout
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Account Settings", style = MaterialTheme.typography.titleMedium)

                TextButton(
                    onClick = {
                        authViewModel.logout(context, app, userData)
                    }
                ) {
                    Text("Logout", color = Color.Red)
                }
            }
        }
    }
}

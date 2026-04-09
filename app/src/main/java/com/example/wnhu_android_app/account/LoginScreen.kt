package com.example.wnhu_android_app

import androidx.compose.runtime.Composable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    app: AppVariables,
    userData: UserData,
    onMicrosoftLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Image(
            painter = painterResource(id = R.drawable.wnhu),
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 24.dp)
        )

        Button(
            onClick = onMicrosoftLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign in with Microsoft")
        }

        TextButton(
            onClick = { app.isGuest = true },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Sign in as Guest")
        }
    }
}

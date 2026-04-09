// @HiltAndroidApp
// class WnhuMobileApp : Application()
package com.example.wnhu_android_app

import com.example.wnhu_mobile_app.ContentView
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier


@Composable
fun WNHUTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFE53935), // your red
            background = Color(0xFF000000),
            surface = Color(0xFF111111),
            onBackground = Color.White,
            onSurface = Color.White
        ),
        content = content
    )
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme()
            ) {
                MainScreen()
            }
        }
    }
}

@Composable
fun WNHUBottomBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        NavigationBarItem(
            selected = selectedTab == "stream",
            onClick = { onTabSelected("stream") },
            icon = { Icon(Icons.Filled.PlayArrow, contentDescription = "Stream") },
            label = { Text("Stream") }
        )

        NavigationBarItem(
            selected = selectedTab == "account",
            onClick = { onTabSelected("account") },
            icon = { Icon(Icons.Filled.Person, contentDescription = "Account") },
            label = { Text("Account") }
        )
    }
}
@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf("stream") }

    Scaffold(
        bottomBar = {
            WNHUBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            val app = remember { AppVariables() }
            val userData = remember { UserData() }

            when (selectedTab) {
                "stream" -> StreamScreen()
                "account" -> AccountScreen(userData, app)
            }

        }
    }
}
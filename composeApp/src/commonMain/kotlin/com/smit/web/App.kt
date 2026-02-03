package com.smit.web

import androidx.compose.runtime.*
import com.smit.web.ui.DashboardScreen
import com.smit.web.ui.LoginScreen
import com.smit.web.ui.theme.DevOpsDashboardTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    DevOpsDashboardTheme(darkTheme = true) {
        // Observe authentication state
        val token by AuthStore.token.collectAsState()
        
        if (token != null) {
            DashboardScreen()
        } else {
            LoginScreen(
                onLoginSuccess = {
                    // State change in AuthStore will automatically trigger recomposition
                }
            )
        }
    }
}
package com.smit.web.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.smit.web.ApiService
import kotlinx.coroutines.launch

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit
) {
    val apiService = remember { ApiService() }
    val coroutineScope = rememberCoroutineScope()
    
    var githubToken by remember { mutableStateOf("") }
    var webhookSecret by remember { mutableStateOf("") }
    var publicHost by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val generatedWebhookUrl by remember {
        derivedStateOf { 
            val host = publicHost.removeSuffix("/")
            if (host.isNotEmpty()) "$host/api/webhooks/github" else ""
        }
    }

    // Load settings
    LaunchedEffect(Unit) {
        try {
            val settings = apiService.getSettings()
            githubToken = settings["GITHUB_TOKEN"] ?: ""
            webhookSecret = settings["WEBHOOK_SECRET"] ?: ""
            publicHost = settings["PUBLIC_HOST"] ?: "http://localhost:8080"
        } catch (e: Exception) {
            error = "Failed to load settings: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    // GitHub Token
                    OutlinedTextField(
                        value = githubToken,
                        onValueChange = { 
                            githubToken = it 
                            error = null
                            successMessage = null
                        },
                        label = { Text("GitHub Personal Access Token") },
                        placeholder = { Text("ghp_...") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Text(
                        text = "Used for fetching branches and triggering workflows.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp, bottom = 16.dp)
                    )

                    // Webhook Secret
                    OutlinedTextField(
                        value = webhookSecret,
                        onValueChange = { 
                            webhookSecret = it 
                            error = null
                            successMessage = null
                        },
                        label = { Text("Webhook Secret") },
                        placeholder = { Text("Optional secret for payload validation") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Public Host
                    OutlinedTextField(
                        value = publicHost,
                        onValueChange = { 
                            publicHost = it 
                            error = null
                            successMessage = null
                        },
                        label = { Text("Public Host / Server URL") },
                        placeholder = { Text("https://your-domain.com") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (generatedWebhookUrl.isNotEmpty()) {
                        Card(
                            modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "GitHub Webhook URL:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = generatedWebhookUrl,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    if (error != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (successMessage != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = successMessage!!,
                            color = Color(0xFF00C853),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isSaving = true
                                error = null
                                successMessage = null
                                try {
                                    apiService.saveSettings(mapOf(
                                        "GITHUB_TOKEN" to githubToken,
                                        "WEBHOOK_SECRET" to webhookSecret,
                                        "PUBLIC_HOST" to publicHost
                                    ))
                                    successMessage = "Settings saved successfully"
                                    kotlinx.coroutines.delay(1000)
                                    onDismiss()
                                } catch (e: Exception) {
                                    error = "Failed to save: ${e.message}"
                                } finally {
                                    isSaving = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Changes")
                        }
                    }
                }
            }
        }
    }
}

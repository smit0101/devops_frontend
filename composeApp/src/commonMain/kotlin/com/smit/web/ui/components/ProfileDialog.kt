package com.smit.web.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.smit.web.ApiService
import com.smit.web.AuthStore
import kotlinx.coroutines.launch

@Composable
fun ProfileDialog(onDismiss: () -> Unit) {
    val username by AuthStore.username.collectAsState()
    val roles by AuthStore.roles.collectAsState()
    
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val apiService = remember { ApiService() }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.width(400.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "User Profile", style = MaterialTheme.typography.headlineSmall)
                        Text(text = username ?: "Unknown", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                        Text(text = roles.joinToString(", "), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                
                Text("Change Password", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text("Old Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (message != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message!!,
                        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (newPassword != confirmPassword) {
                            message = "New passwords do not match"
                            isError = true
                            return@Button
                        }
                        
                        isLoading = true
                        message = null
                        scope.launch {
                            try {
                                apiService.changePassword(oldPassword, newPassword)
                                message = "Password updated successfully"
                                isError = false
                                oldPassword = ""
                                newPassword = ""
                                confirmPassword = ""
                            } catch (e: Exception) {
                                message = "Error: ${e.message}"
                                isError = true
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && oldPassword.isNotEmpty() && newPassword.isNotEmpty()
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp)) else Text("Update Password")
                }
            }
        }
    }
}

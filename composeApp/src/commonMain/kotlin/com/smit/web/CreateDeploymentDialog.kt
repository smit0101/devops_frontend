package com.smit.web

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun CreateDeploymentDialog(
    templateDeployment: Deployment? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String?) -> Unit
) {
    val apiService = remember { ApiService() }
    val scope = rememberCoroutineScope()
    
    var name by remember { mutableStateOf(templateDeployment?.name ?: "") }
    var description by remember { mutableStateOf(templateDeployment?.description ?: "") }
    var repositoryUrl by remember { mutableStateOf(templateDeployment?.repositoryUrl ?: "") }
    var branch by remember { mutableStateOf(templateDeployment?.branch ?: "main") }
    var serviceUrl by remember { mutableStateOf(templateDeployment?.serviceUrl ?: "") }
    
    var branches by remember { mutableStateOf(listOf(branch)) }
    var isFetchingBranches by remember { mutableStateOf(false) }

    // Fetch branches if template provided
    LaunchedEffect(repositoryUrl) {
        if (repositoryUrl.contains("github.com/")) {
            isFetchingBranches = true
            try {
                val fetched = apiService.getBranches(repositoryUrl)
                if (fetched.isNotEmpty()) {
                    branches = fetched
                }
            } catch (e: Exception) {}
            finally { isFetchingBranches = false }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (templateDeployment != null) "Redeploy ${templateDeployment.name}" else "New Deployment") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = repositoryUrl,
                    onValueChange = { 
                        repositoryUrl = it 
                        if (it.contains("github.com/")) {
                            isFetchingBranches = true
                            scope.launch {
                                try {
                                    val fetched = apiService.getBranches(it)
                                    if (fetched.isNotEmpty()) {
                                        branches = fetched
                                        branch = fetched.first()
                                    }
                                } catch (e: Exception) {
                                    // Fallback to manual
                                } finally {
                                    isFetchingBranches = false
                                }
                            }
                        }
                    },
                    label = { Text("GitHub Repository URL") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("https://github.com/user/repo") },
                    trailingIcon = {
                        if (isFetchingBranches) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = serviceUrl,
                    onValueChange = { serviceUrl = it },
                    label = { Text("Service URL (for health checks)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("https://my-app.com/health") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Simple Branch Selection
                Text("Select Branch", style = MaterialTheme.typography.labelSmall)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    branches.take(3).forEach { b ->
                        FilterChip(
                            selected = branch == b,
                            onClick = { branch = b },
                            label = { Text(b) }
                        )
                    }
                    if (branches.size > 3) {
                        Text("...", modifier = Modifier.align(Alignment.CenterVertically))
                    }
                }
                
                if (branches.isEmpty() || !repositoryUrl.contains("github.com")) {
                    OutlinedTextField(
                        value = branch,
                        onValueChange = { branch = it },
                        label = { Text("Manual Branch") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && repositoryUrl.isNotBlank()) {
                        onConfirm(name, description, repositoryUrl, branch, if (serviceUrl.isBlank()) null else serviceUrl)
                    }
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

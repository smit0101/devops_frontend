package com.smit.web

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun LogViewerDialog(
    deployment: Deployment,
    onDismiss: () -> Unit
) {
    val apiService = remember { ApiService() }
    var jobs by remember { mutableStateOf<List<GitHubJob>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            try {
                if (deployment.workflowRunId != null) {
                    jobs = apiService.getDeploymentJobs(deployment.id)
                    if (jobs.isNotEmpty()) isLoading = false
                }
            } catch (e: Exception) {
                // Keep trying
            }
            if (deployment.status == DeploymentStatus.COMPLETED || deployment.status == DeploymentStatus.FAILED) {
                // Final check to get last logs
                try {
                    if (deployment.workflowRunId != null) {
                        jobs = apiService.getDeploymentJobs(deployment.id)
                    }
                } catch (e: Exception) {}
                isLoading = false
                break
            }
            delay(5000)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxSize().padding(16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Deployment Logs: ${deployment.name}", modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
            }
        },
        text = {
            Box(Modifier.fillMaxSize()) {
                if (deployment.workflowRunId == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("Waiting for GitHub to start the action...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else if (isLoading && jobs.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (jobs.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No job details found yet. Polling GitHub API...", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        jobs.forEach { job ->
                            item {
                                Text("Job: ${job.name} (${job.status})", 
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(job.steps) { step ->
                                StepItem(step)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun StepItem(step: GitHubStep) {
    val icon = when (step.conclusion) {
        "success" -> Icons.Default.CheckCircle
        "failure" -> Icons.Default.Error
        else -> Icons.Default.Pending
    }
    val color = when (step.conclusion) {
        "success" -> Color(0xFF00C853)
        "failure" -> Color(0xFFFF1744)
        else -> Color.Gray
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(12.dp))
        Text(
            text = step.name,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = step.status,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

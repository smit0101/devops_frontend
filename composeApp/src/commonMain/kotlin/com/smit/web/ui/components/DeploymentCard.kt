package com.smit.web.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.smit.web.Deployment
import com.smit.web.DeploymentStatus
import com.smit.web.HealthStatus
import com.smit.web.util.TimeUtil
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.delay

@Composable
fun DeploymentCard(
    deployment: Deployment,
    onDeleteClick: (Long) -> Unit,
    onStatusUpdateClick: (Long, DeploymentStatus) -> Unit,
    onLogsClick: (Deployment) -> Unit,
    onRedeployClick: (Deployment) -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val isRunning = deployment.status == DeploymentStatus.IN_PROGRESS
    
    val statusColor = when (deployment.status) {
        DeploymentStatus.COMPLETED -> Color(0xFF00C853)
        DeploymentStatus.PENDING -> Color(0xFF6200EE)
        DeploymentStatus.IN_PROGRESS -> Color(0xFF2979FF)
        DeploymentStatus.FAILED -> Color(0xFFFF1744)
    }

    val healthColor = when (deployment.healthStatus) {
        HealthStatus.HEALTHY -> Color(0xFF00C853)
        HealthStatus.UNHEALTHY -> Color(0xFFFF1744)
        HealthStatus.UNKNOWN -> Color(0xFF757575)
    }

    val healthIcon = when (deployment.healthStatus) {
        HealthStatus.HEALTHY -> Icons.Default.CheckCircle
        HealthStatus.UNHEALTHY -> Icons.Default.Warning
        HealthStatus.UNKNOWN -> Icons.Default.QuestionMark
    }

    // Border Animation only if strictly IN_PROGRESS
    val infiniteTransition = rememberInfiniteTransition()
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Progress and Step Text derived STRICTLY from status
    val progressValue = when (deployment.status) {
        DeploymentStatus.PENDING -> 0.05f
        DeploymentStatus.IN_PROGRESS -> 0.65f
        DeploymentStatus.COMPLETED -> 1.0f
        DeploymentStatus.FAILED -> 1.0f
    }
    
    val stepText = when (deployment.status) {
        DeploymentStatus.PENDING -> "Waiting for GitHub..."
        DeploymentStatus.IN_PROGRESS -> "Action Running on GitHub..."
        DeploymentStatus.COMPLETED -> "Deployed Successfully"
        DeploymentStatus.FAILED -> "Workflow Failed"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (isRunning) statusColor.copy(alpha = borderAlpha) else statusColor.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = deployment.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = deployment.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Health Indicator
                    if (deployment.serviceUrl != null) {
                        Surface(
                            onClick = { uriHandler.openUri(deployment.serviceUrl) },
                            color = healthColor.copy(alpha = 0.1f),
                            contentColor = healthColor,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(healthIcon, null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = deployment.healthStatus.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        contentColor = statusColor,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            if (isRunning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 2.dp,
                                    color = statusColor
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = deployment.status.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stepText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "${(progressValue * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (deployment.status == DeploymentStatus.FAILED) MaterialTheme.colorScheme.error else statusColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progressValue },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = if (deployment.status == DeploymentStatus.FAILED) MaterialTheme.colorScheme.error else statusColor,
                    trackColor = statusColor.copy(alpha = 0.1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Updated: ${TimeUtil.formatTimeAgo(deployment.updatedAt ?: "")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )

                Row {
                    IconButton(onClick = { onRedeployClick(deployment) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Redeploy / Rollback",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = { onLogsClick(deployment) }) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "View Logs",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (deployment.repositoryUrl != null) {
                        IconButton(onClick = { uriHandler.openUri(deployment.repositoryUrl + "/actions") }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = "View on GitHub",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    if (deployment.status != DeploymentStatus.COMPLETED && deployment.status != DeploymentStatus.FAILED) {
                        IconButton(onClick = {
                            val nextStatus = when (deployment.status) {
                                DeploymentStatus.PENDING -> DeploymentStatus.IN_PROGRESS
                                DeploymentStatus.IN_PROGRESS -> DeploymentStatus.COMPLETED
                                DeploymentStatus.COMPLETED -> DeploymentStatus.COMPLETED
                                DeploymentStatus.FAILED -> DeploymentStatus.FAILED
                            }
                            onStatusUpdateClick(deployment.id, nextStatus)
                        }) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Advance",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    IconButton(onClick = { onDeleteClick(deployment.id) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

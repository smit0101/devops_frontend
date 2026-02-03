package com.smit.web

import kotlinx.serialization.Serializable

@Serializable
data class Deployment(
    val id: Long,
    val name: String,
    val description: String,
    val repositoryUrl: String? = null,
    val branch: String? = "main",
    val serviceUrl: String? = null,
    val workflowRunId: Long? = null,
    val status: DeploymentStatus,
    val healthStatus: HealthStatus = HealthStatus.UNKNOWN,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
enum class DeploymentStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}

@Serializable
enum class HealthStatus {
    HEALTHY,
    UNHEALTHY,
    UNKNOWN
}

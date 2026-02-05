package com.smit.web

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class DeploymentRequest(
    val name: String,
    val description: String,
    val repositoryUrl: String,
    val branch: String,
    val serviceUrl: String? = null,
    val status: DeploymentStatus
)

@Serializable
data class GitHubJob(
    val name: String,
    val status: String,
    val conclusion: String? = null,
    val steps: List<GitHubStep> = emptyList()
)

@Serializable
data class GitHubStep(
    val name: String,
    val status: String,
    val conclusion: String? = null,
    val number: Int
)

@Serializable
data class GitHubJobsResponse(
    val jobs: List<GitHubJob> = emptyList()
)

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class ChangePasswordRequest(val oldPassword: String, val newPassword: String)

@Serializable
data class AuthResponse(val token: String, val username: String, val roles: Set<String> = emptySet())

class ApiService {
    private val client = HttpClient {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }
    }

    private val baseUrl = "http://localhost:8080/api/deployments"
    private val authUrl = "http://localhost:8080/api/auth"

    // Helper to add auth header
    private suspend fun <T> authorizedRequest(block: suspend () -> T): T {
        return block()
    }
    
    // We need to manually add headers for now since we aren't using a globally installed Auth plugin
    // In a future refactor, we can install the Auth plugin to the HttpClient
    
    suspend fun login(username: String, password: String): AuthResponse {
        val response = client.post("$authUrl/login") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(LoginRequest(username, password))
        }
        println("Login response status: ${response.status}")
        return response.body()
    }

    suspend fun register(username: String, password: String): AuthResponse {
        val response = client.post("$authUrl/register") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(LoginRequest(username, password))
        }
        println("Register response status: ${response.status}")
        return response.body()
    }
    
    suspend fun changePassword(old: String, new: String) {
        client.post("$authUrl/change-password") {
            header("Authorization", "Bearer ${AuthStore.getToken()}")
            contentType(ContentType.Application.Json)
            setBody(ChangePasswordRequest(old, new))
        }
    }

    suspend fun getDeployments(): List<Deployment> {
        return client.get(baseUrl) {
             header("Authorization", "Bearer ${AuthStore.getToken()}")
        }.body()
    }

    suspend fun getBranches(repoUrl: String): List<String> {
        return client.get("$baseUrl/branches") {
            header("Authorization", "Bearer ${AuthStore.getToken()}")
            parameter("repoUrl", repoUrl)
        }.body()
    }

    suspend fun getDeploymentJobs(id: Long): List<GitHubJob> {
        val response: GitHubJobsResponse = client.get("$baseUrl/$id/jobs") {
            header("Authorization", "Bearer ${AuthStore.getToken()}")
        }.body()
        return response.jobs
    }

    suspend fun createDeployment(name: String, description: String, repositoryUrl: String, branch: String, serviceUrl: String? = null): Deployment {
        val request = DeploymentRequest(name, description, repositoryUrl, branch, serviceUrl, DeploymentStatus.PENDING)
        return client.post(baseUrl) {
            header("Authorization", "Bearer ${AuthStore.getToken()}")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun deleteDeployment(id: Long) {
        client.delete("$baseUrl/$id") {
            header("Authorization", "Bearer ${AuthStore.getToken()}")
        }
    }

    suspend fun updateDeploymentStatus(id: Long, status: DeploymentStatus): Deployment {
        return client.patch("$baseUrl/$id/status") {
            header("Authorization", "Bearer ${AuthStore.getToken()}")
            parameter("status", status)
        }.body()
    }

    suspend fun getSettings(): Map<String, String> {
        return client.get("http://localhost:8080/api/settings") {
            header("Authorization", "Bearer ${AuthStore.getToken()}")
        }.body()
    }

    suspend fun saveSettings(settings: Map<String, String>) {
        client.post("http://localhost:8080/api/settings") {
            header("Authorization", "Bearer ${AuthStore.getToken()}")
            contentType(ContentType.Application.Json)
            setBody(settings)
        }
    }
}

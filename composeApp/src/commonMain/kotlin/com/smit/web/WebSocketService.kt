package com.smit.web

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

@Serializable
data class WebSocketMessage(
    val type: String,
    val payload: JsonElement
)

class WebSocketService {

    private val client = HttpClient {
        install(WebSockets)
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private val _deployments = MutableSharedFlow<Deployment>()
    val deployments: SharedFlow<Deployment> = _deployments.asSharedFlow()

    private val _deploymentDeletions = MutableSharedFlow<Long>()
    val deploymentDeletions: SharedFlow<Long> = _deploymentDeletions.asSharedFlow()

    suspend fun connectAndObserveDeployments() {
        println("Attempting to connect to WebSocket: ws://localhost:8080/ws")
        client.webSocket(method = HttpMethod.Get, host = "localhost", port = 8080, path = "/ws") {
            println("WebSocket connected to ws://localhost:8080/ws")
            while (isActive) {
                val message = incoming.receive()
                if (message is Frame.Text) {
                    val receivedText = message.readText()
                    try {
                        val wsMessage = Json.decodeFromString<WebSocketMessage>(receivedText)
                        when (wsMessage.type) {
                            "UPDATE" -> {
                                val deployment = Json.decodeFromJsonElement<Deployment>(wsMessage.payload)
                                _deployments.emit(deployment)
                            }
                            "DELETE" -> {
                                val id = Json.decodeFromJsonElement<Long>(wsMessage.payload)
                                _deploymentDeletions.emit(id)
                            }
                        }
                    } catch (e: Exception) {
                        println("ERROR: [WS] Parse failure: ${e.message}")
                    }
                }
            }
        }
    }
}
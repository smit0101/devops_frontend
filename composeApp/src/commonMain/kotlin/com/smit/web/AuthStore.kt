package com.smit.web

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object AuthStore {
    private val _token = MutableStateFlow<String?>(null)
    val token = _token.asStateFlow()

    private val _username = MutableStateFlow<String?>(null)
    val username = _username.asStateFlow()

    private val _roles = MutableStateFlow<Set<String>>(emptySet())
    val roles = _roles.asStateFlow()

    fun setSession(token: String, username: String, roles: Set<String>) {
        _token.value = token
        _username.value = username
        _roles.value = roles
        // In a real app, persist to local storage/preferences here
    }

    fun clearSession() {
        _token.value = null
        _username.value = null
        _roles.value = emptySet()
    }

    fun isAuthenticated(): Boolean {
        return _token.value != null
    }
    
    fun isAdmin(): Boolean {
        return _roles.value.contains("ROLE_ADMIN")
    }
    
    fun getToken(): String? = _token.value
}

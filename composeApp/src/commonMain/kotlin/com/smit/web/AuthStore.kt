package com.smit.web

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object AuthStore {
    private val _token = MutableStateFlow<String?>(PlatformSettings.getToken())
    val token = _token.asStateFlow()

    private val _username = MutableStateFlow<String?>(PlatformSettings.getUsername())
    val username = _username.asStateFlow()

    private val _roles = MutableStateFlow<Set<String>>(PlatformSettings.getRoles())
    val roles = _roles.asStateFlow()

    fun setSession(token: String, username: String, roles: Set<String>) {
        _token.value = token
        _username.value = username
        _roles.value = roles
        
        PlatformSettings.saveToken(token)
        PlatformSettings.saveUsername(username)
        PlatformSettings.saveRoles(roles)
    }

    fun clearSession() {
        _token.value = null
        _username.value = null
        _roles.value = emptySet()
        
        PlatformSettings.clearToken()
        PlatformSettings.clearUsername()
        PlatformSettings.clearRoles()
    }

    fun isAuthenticated(): Boolean {
        return _token.value != null
    }
    
    fun isAdmin(): Boolean {
        return _roles.value.contains("ROLE_ADMIN")
    }
    
    fun getToken(): String? = _token.value
}

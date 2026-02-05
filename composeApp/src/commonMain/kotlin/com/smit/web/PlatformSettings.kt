package com.smit.web

expect object PlatformSettings {
    fun saveToken(token: String)
    fun getToken(): String?
    fun clearToken()
    
    fun saveUsername(username: String)
    fun getUsername(): String?
    fun clearUsername()

    fun saveRoles(roles: Set<String>)
    fun getRoles(): Set<String>
    fun clearRoles()
}

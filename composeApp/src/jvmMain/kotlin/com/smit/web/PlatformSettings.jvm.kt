package com.smit.web

import java.util.prefs.Preferences

actual object PlatformSettings {
    private val prefs = Preferences.userNodeForPackage(AuthStore::class.java)

    actual fun saveToken(token: String) {
        prefs.put("auth_token", token)
    }

    actual fun getToken(): String? {
        return prefs.get("auth_token", null)
    }

    actual fun clearToken() {
        prefs.remove("auth_token")
    }

    actual fun saveUsername(username: String) {
        prefs.put("auth_username", username)
    }

    actual fun getUsername(): String? {
        return prefs.get("auth_username", null)
    }

    actual fun clearUsername() {
        prefs.remove("auth_username")
    }

    actual fun saveRoles(roles: Set<String>) {
        prefs.put("auth_roles", roles.joinToString(","))
    }

    actual fun getRoles(): Set<String> {
        val rolesStr = prefs.get("auth_roles", "")
        if (rolesStr.isBlank()) return emptySet()
        return rolesStr.split(",").toSet()
    }

    actual fun clearRoles() {
        prefs.remove("auth_roles")
    }
}

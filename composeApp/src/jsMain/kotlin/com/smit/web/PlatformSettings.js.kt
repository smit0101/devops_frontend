package com.smit.web

import kotlinx.browser.localStorage

actual object PlatformSettings {
    actual fun saveToken(token: String) {
        localStorage.setItem("auth_token", token)
    }

    actual fun getToken(): String? {
        // localStorage.getItem returns String? or null.
        return localStorage.getItem("auth_token")
    }

    actual fun clearToken() {
        localStorage.removeItem("auth_token")
    }

    actual fun saveUsername(username: String) {
        localStorage.setItem("auth_username", username)
    }

    actual fun getUsername(): String? {
        return localStorage.getItem("auth_username")
    }

    actual fun clearUsername() {
        localStorage.removeItem("auth_username")
    }

    actual fun saveRoles(roles: Set<String>) {
        localStorage.setItem("auth_roles", roles.joinToString(","))
    }

    actual fun getRoles(): Set<String> {
        val rolesStr = localStorage.getItem("auth_roles")
        if (rolesStr.isNullOrBlank()) return emptySet()
        return rolesStr.split(",").toSet()
    }

    actual fun clearRoles() {
        localStorage.removeItem("auth_roles")
    }
}

package com.smit.web

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
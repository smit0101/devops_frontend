package com.smit.web.util

expect object DateTimeUtil {
    fun nowMillis(): Long
    fun parseIsoToMillis(isoString: String): Long
}

package com.smit.web.util

import kotlin.js.Date

actual object DateTimeUtil {
    actual fun nowMillis(): Long = Date.now().toLong()

    actual fun parseIsoToMillis(isoString: String): Long {
        if (isoString.isEmpty()) return 0
        // JS Date.parse is very flexible with ISO formats
        val normalized = if (isoString.contains("Z")) isoString else isoString + "Z"
        val time = Date.parse(normalized)
        return if (time.isNaN()) 0 else time.toLong()
    }
}

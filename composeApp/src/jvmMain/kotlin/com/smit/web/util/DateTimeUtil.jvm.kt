package com.smit.web.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

actual object DateTimeUtil {
    actual fun nowMillis(): Long = System.currentTimeMillis()

    actual fun parseIsoToMillis(isoString: String): Long {
        if (isoString.isEmpty()) return 0
        return try {
            // Normalize: Ensure it ends with Z for Instant parsing
            val normalized = if (isoString.endsWith("Z")) isoString else isoString + "Z"
            Instant.parse(normalized).toEpochMilli()
        } catch (e: Exception) {
            try {
                // Fallback for strings with fractional seconds but no Z
                val clean = isoString.split(".")[0].split("Z")[0]
                LocalDateTime.parse(clean, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .toInstant(ZoneOffset.UTC)
                    .toEpochMilli()
            } catch (e2: Exception) {
                0
            }
        }
    }
}

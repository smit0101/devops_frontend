package com.smit.web.util

object TimeUtil {
    fun formatIsoDate(isoString: String): String {
        if (isoString.isEmpty()) return "Unknown"
        // Simple manual formatting to avoid heavy dependencies
        // Input: 2026-01-04T12:44:03...
        return try {
            val parts = isoString.split("T")
            val date = parts[0]
            val time = parts[1].split(".")[0].take(5) // Get HH:mm
            "$date $time"
        } catch (e: Exception) {
            isoString
        }
    }

    fun formatTimeAgo(isoString: String): String {
        if (isoString.isEmpty()) return "Unknown"
        try {
            val pastMillis = DateTimeUtil.parseIsoToMillis(isoString)
            val nowMillis = DateTimeUtil.nowMillis()
            val diffSeconds = (nowMillis - pastMillis) / 1000

            return when {
                diffSeconds < 60 -> "just now"
                diffSeconds < 3600 -> "${diffSeconds / 60}m ago"
                diffSeconds < 86400 -> "${diffSeconds / 3600}h ago"
                diffSeconds < 604800 -> "${diffSeconds / 86400}d ago"
                else -> formatIsoDate(isoString)
            }
        } catch (e: Exception) {
            return formatIsoDate(isoString)
        }
    }
}
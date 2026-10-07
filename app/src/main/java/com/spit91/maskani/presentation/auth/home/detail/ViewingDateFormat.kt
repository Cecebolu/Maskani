package com.spit91.maskani.presentation.auth.home.detail

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private val utc = TimeZone.getTimeZone("UTC")

// "Sat, 10 Oct 2026" from DatePicker millis (midnight UTC)
fun formatViewingDate(utcMillis: Long): String =
    SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).apply { timeZone = utc }.format(utcMillis)

// "Sat, 10 Oct 2026" from a stored "2026-10-10"; falls back to the raw text if it can't be parsed
fun formatViewingDate(storedDate: String): String {
    val parsed = runCatching {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = utc }.parse(storedDate)
    }.getOrNull() ?: return storedDate
    return formatViewingDate(parsed.time)
}

// today's local date expressed as midnight UTC, which is how DatePicker represents days
fun todayAsUtcMillis(): Long {
    val local = Calendar.getInstance()
    return Calendar.getInstance(utc).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

package ru.binaryblitz.Chisto.Utils

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    fun getDateStringRepresentationWithoutTime(date: Date?): String {
        if (date == null) return ""
        val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        format.timeZone = TimeZone.getDefault()
        return format.format(date)
    }

    fun getTimeStringRepresentation(date: Date?): String {
        if (date == null) return ""
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("UTC")
        return format.format(date)
    }

    fun parse(input: String): Date {
        return DateTime(input, DateTimeZone.getDefault()).toDate()
    }
}

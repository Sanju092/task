package com.yearplanner.data.db.converters

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Year
import java.time.YearMonth
import java.time.ZoneId

class Converters {

    @TypeConverter
    fun fromLocalDate(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun toLocalDate(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    @TypeConverter
    fun fromZoneId(value: String?): ZoneId? {
        return value?.let { ZoneId.of(it) }
    }

    @TypeConverter
    fun toZoneId(zoneId: ZoneId?): String? {
        return zoneId?.id
    }

    @TypeConverter
    fun fromYear(value: Int?): Year? {
        return value?.let { Year.of(it) }
    }

    @TypeConverter
    fun toYear(year: Year?): Int? {
        return year?.value
    }
}

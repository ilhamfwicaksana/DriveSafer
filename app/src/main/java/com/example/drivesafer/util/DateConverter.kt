package com.example.drivesafer.util

import androidx.room.TypeConverter
import java.util.*

/**
 * Converter untuk mengubah Date menjadi Long (timestamp) dan sebaliknya
 * agar dapat disimpan di database Room
 */
class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
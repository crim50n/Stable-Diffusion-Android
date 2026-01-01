package dev.minios.pdaiv1.storage.converters

import androidx.room.TypeConverter
import java.util.*

internal class DateConverters {

    @TypeConverter
    fun dateToLong(date: Date): Long = date.time

    @TypeConverter
    fun longToDate(ts: Long): Date = Date(ts)
}

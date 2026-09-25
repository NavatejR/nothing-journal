package com.nothingjournal.data.local

import androidx.room.TypeConverter
import com.nothingjournal.data.model.EntryKind
import com.nothingjournal.data.model.EntrySource

class Converters {
    @TypeConverter
    fun entryKindToString(kind: EntryKind): String = kind.name

    @TypeConverter
    fun stringToEntryKind(value: String): EntryKind =
        EntryKind.valueOf(value)

    @TypeConverter
    fun entrySourceToString(source: EntrySource): String = source.name

    @TypeConverter
    fun stringToEntrySource(value: String): EntrySource =
        EntrySource.valueOf(value)
}

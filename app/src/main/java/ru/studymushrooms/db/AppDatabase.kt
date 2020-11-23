package ru.studymushrooms.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = arrayOf(Mushroom::class, MushroomPlace::class, Note::class), version = 1)
@TypeConverters(TiviTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mushroomDao(): MushroomDao
    abstract fun mushroomPlaceDao(): MushroomPlaceDao
    abstract fun notesDao(): NotesDao
}
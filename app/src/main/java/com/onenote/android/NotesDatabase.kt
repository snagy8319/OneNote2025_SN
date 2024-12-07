package com.onenote.android

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Database class for the Notes application.
 * Defines the database configuration and serves as the app's main access point to the persisted data.
 */
@Database(entities = [Note::class], version = 1)
abstract class NotesDatabase : RoomDatabase() {
    /**
     * Returns the DAO for accessing the Note database.
     * @return The NoteDao instance.
     */
    abstract fun noteDao(): NoteDao
}
package com.onenote.android

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Data class representing a Note entity in the database.
 * @param title The title of the note.
 * @param message The message content of the note.
 * @param image The file path of the image associated with the note.
 * @param latitude The latitude of the note's location.
 * @param longitude The longitude of the note's location.
 * @param id The unique identifier of the note.
 */
@Entity(tableName = "notes")
data class Note(
    @ColumnInfo(name="title") var title: String,
    @ColumnInfo(name="message") var message: String,
    @ColumnInfo(name="image") var image: String,
    @ColumnInfo(name="latitude") var latitude: Double? = null,
    @ColumnInfo(name="longitude") var longitude: Double? = null,
    @PrimaryKey(autoGenerate = true) var id: Int = 0)

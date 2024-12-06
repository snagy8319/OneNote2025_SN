package com.onenote.android

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class

Note(
    @ColumnInfo(name="title") var title: String,
    @ColumnInfo(name="message") var message: String,
    @ColumnInfo(name="image") var image: String,
    @ColumnInfo(name="latitude") var latitude: Double? = null,
    @ColumnInfo(name="longitude") var longitude: Double? = null,
    @PrimaryKey(autoGenerate = true) var id: Int = 0)

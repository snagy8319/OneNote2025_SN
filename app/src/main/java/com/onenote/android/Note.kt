package com.onenote.android

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class

Note(
    @ColumnInfo(name="title") var title: String,
    @ColumnInfo(name="message") var message: String,
    @PrimaryKey(autoGenerate = true) var id: Int = 0)

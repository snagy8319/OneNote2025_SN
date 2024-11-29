package com.onenote.android

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update


@Dao
interface NoteDao {

    @Query("SELECT * FROM notes")
    fun getAll(): List<Note>

    @Insert
    fun insertAll(vararg notes: Note)

    @Delete
    fun delete(note: Note)

    @Update
    fun update(note: Note)

    @Query("SELECT * FROM notes WHERE id IS (:id)")
    fun loadAllByIds(id: Int): List<Note>
}
package com.onenote.android

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * Data Access Object (DAO) for accessing the Note database.
 * Provides methods for performing CRUD operations on the notes table.
 */
@Dao
interface NoteDao {

    /**
     * Retrieves all notes from the database.
     * @return A list of all notes.
     */
    @Query("SELECT * FROM notes")
    fun getAll(): List<Note>

    /**
     * Inserts multiple notes into the database.
     * @param notes The notes to be inserted.
     */
    @Insert
    fun insertAll(vararg notes: Note)

    /**
     * Deletes a note from the database.
     * @param note The note to be deleted.
     */
    @Delete
    fun delete(note: Note)

    /**
     * Updates a note in the database.
     * @param note The note to be updated.
     */
    @Update
    fun update(note: Note)

    /**
     * Retrieves notes by their ID.
     * @param id The ID of the note to be retrieved.
     * @return A list of notes with the specified ID.
     */
    @Query("SELECT * FROM notes WHERE id IS (:id)")
    fun loadAllByIds(id: Int): List<Note>
}
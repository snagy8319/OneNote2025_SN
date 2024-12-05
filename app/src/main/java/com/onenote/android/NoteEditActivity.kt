package com.onenote.android

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteEditActivity : AppCompatActivity() {

    private lateinit var noteDao: NoteDao
    private lateinit var noteEditTitle: EditText
    private lateinit var noteEditMessage: EditText
    private lateinit var buttonSave: Button
    private var noteId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_edit)

        // Set up toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)

        // Initialize Room DB
        val db = Room.databaseBuilder(
            applicationContext,
            NotesDatabase::class.java, "notes"
        ).allowMainThreadQueries().build()
        noteDao = db.noteDao()

        // Find views by ID
        val noteEditTitle = findViewById<EditText>(R.id.noteEditTitle)
        val noteEditMessage = findViewById<EditText>(R.id.noteEditMessage)
        val buttonSave = findViewById<Button>(R.id.buttonSave)

        // Check if we are editing an existing note
        noteId = intent.getIntExtra("noteId", -1)


        // Load note if editing
        if (noteId != -1) {
            CoroutineScope(Dispatchers.IO).launch {
                val note = noteDao.loadAllByIds(noteId).firstOrNull()
                note?.let {
                    runOnUiThread {
                        noteEditTitle.setText(it.title)
                        noteEditMessage.setText(it.message)
                    }
                }
            }
        }

        // Set OnClickListener
        buttonSave.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val note = Note(
                    noteEditTitle.editableText.toString(),
                    noteEditMessage.editableText.toString(),
                )
                if (noteId != -1) {
                    note.id = noteId
                    noteDao.update(note)
                } else {
                    noteDao.insertAll(note)
                }
                runOnUiThread {
                    Toast.makeText(
                        this@NoteEditActivity,
                        "Note saved",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        } else if (item.itemId == R.id.delete) {
            showDeleteDialog()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.delete_message))
            .setPositiveButton(R.string.yes) { _, _ ->
                val noteId = intent.getIntExtra("noteId", -1)
                if (noteId != -1) {
                    val note = noteDao.loadAllByIds(noteId).firstOrNull()
                    note?.let {
                        noteDao.delete(it)
                    }
                }
                finish()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }
}
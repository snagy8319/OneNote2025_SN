package com.onenote.android

import android.util.Log
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteListActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var noteDao: NoteDao
    private lateinit var adapter: NoteAdapter
    private var selectedNoteId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_list)

        // Set up toolbar
        setSupportActionBar(findViewById(R.id.toolbar))

        // Find views by ID
        listView = findViewById(R.id.listView)

        // Initialize Room DB
        val db = Room.databaseBuilder(
            applicationContext,
            NotesDatabase::class.java, "notes"
        ).allowMainThreadQueries().build()
        noteDao = db.noteDao()
        adapter = NoteAdapter(this, noteDao.getAll(), selectedNoteId)


        // Load notes asynchronously with error handling
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val notes = noteDao.getAll()
                runOnUiThread {
                    // Update UI with notes
                    adapter.notes = notes
                    listView.adapter = adapter

                }
            } catch (e: Exception) {
                Log.e("NoteListActivity", "Error loading notes", e)
                runOnUiThread {
                    Toast.makeText(this@NoteListActivity, "Error loading notes", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Set item click listener
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            selectedNoteId = adapter.getItemId(position).toInt()
            adapter.selectedNoteId = selectedNoteId
            adapter.notifyDataSetChanged()
        }
    }

    override fun onResume() {
        super.onResume()


        // Reload notes asynchronously with error handling
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val notes = noteDao.getAll()
                runOnUiThread {
                    adapter.notes = notes
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Log.e("NoteListActivity", "Error reloading notes", e)
                runOnUiThread {
                    Toast.makeText(this@NoteListActivity, "Error reloading notes", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_list, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        R.id.add -> {
            val intent = Intent(this, NoteEditActivity::class.java)
            startActivity(intent)
            true
        }
        R.id.edit -> {
            if (selectedNoteId != -1) {
                val intent = Intent(this, NoteEditActivity::class.java)
                intent.putExtra("noteId", selectedNoteId)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please select a note to edit", Toast.LENGTH_SHORT).show()
            }
            true
        }
        R.id.delete -> {
            showDeleteDialog()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}

    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.delete_message))
            .setPositiveButton(R.string.yes) { _, _ ->
                val noteId = intent.getIntExtra("noteId", -1)
                if (noteId != -1) {
                    try {
                        val note = noteDao.loadAllByIds(noteId).firstOrNull()
                        note?.let {
                            noteDao.delete(it)
                        }
                        runOnUiThread {
                            Toast.makeText(this@NoteListActivity, "Note deleted", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("NoteListActivity", "Error deleting note", e)
                        runOnUiThread {
                            Toast.makeText(this@NoteListActivity, "Error deleting note", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                finish()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }
}
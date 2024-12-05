package com.onenote.android

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.room.Room

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

        // Set adapter
        listView.adapter = adapter

        // Set item click listener
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            selectedNoteId = adapter.getItemId(position).toInt()
            adapter.selectedNoteId = selectedNoteId
            adapter.notifyDataSetChanged()
        }
    }

    override fun onResume() {
        super.onResume()

        // Reload notes
        adapter.notes = noteDao.getAll()
        adapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_list, menu)
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
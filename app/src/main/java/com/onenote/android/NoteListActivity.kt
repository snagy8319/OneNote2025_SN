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
        adapter = NoteAdapter(this, noteDao.getAll())

        // Set adapter
        listView.setAdapter(adapter)

        // Set item click listener
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            selectedNoteId = adapter.getItemId(position).toInt()
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
        when (item.itemId) {
            R.id.add -> {
                // Open NoteEditActivity
                val intent = Intent(this, NoteEditActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.edit -> {
                if (selectedNoteId != -1) {
                    // Open NoteEditActivity with the selected note's details
                    val intent = Intent(this, NoteEditActivity::class.java).apply {
                        putExtra("noteId", selectedNoteId)
                    }
                    startActivity(intent)
                    return true
                } else {
                    // Show a message if no note is selected
                    Toast.makeText(this, "Please select a note to edit", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
package com.onenote.android

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class NoteListActivity : AppCompatActivity() {

    lateinit var noteTitle: TextView
    lateinit var noteMessage: TextView
    lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_list)

        // Set up toolbar
        setSupportActionBar(findViewById(R.id.toolbar))

        // Find views by ID
        noteTitle = findViewById(R.id.noteTitle)
        noteMessage = findViewById(R.id.noteMessage)
        listView = findViewById(R.id.listView)

        // Simple ListView example
        val title = Preferences(this).getNoteTitle()
        val items = arrayOf(title, title, title, title, title, title, title, title, title, title, title, title, title, title, title, title, title, title)
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1, android.R.id.text1, items
        )
        listView.setAdapter(adapter)
    }

    override fun onResume() {
        super.onResume()

        noteTitle.text = Preferences(this).getNoteTitle()
        noteMessage.text = Preferences(this).getNoteMessage()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_list, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add) {

            // Open NoteEditActivity
            val intent = Intent(this, NoteEditActivity::class.java)
            startActivity(intent)
        }

        return super.onOptionsItemSelected(item)
    }
}
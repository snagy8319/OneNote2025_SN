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

class NoteEditActivity : AppCompatActivity() {

    private lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_edit)

        // Init Preferences
        preferences = Preferences(this)

        // Set up toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)

        // Find views by ID
        val noteEditTitle = findViewById<EditText>(R.id.noteEditTitle)
        val noteEditMessage = findViewById<EditText>(R.id.noteEditMessage)
        val buttonSave = findViewById<Button>(R.id.buttonSave)

        // Set title and message if available
        noteEditTitle.setText(preferences.getNoteTitle())
        noteEditMessage.setText(preferences.getNoteMessage())

        // Set OnClickListener
        buttonSave.setOnClickListener{
            Toast.makeText(this, R.string.saved, Toast.LENGTH_LONG).show()

            preferences.setNoteMessage(noteEditMessage.editableText.toString())
            preferences.setNoteTitle(noteEditTitle.editableText.toString())

            finish()
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
                preferences.setNoteTitle(null)
                preferences.setNoteMessage(null)
                finish()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }
}
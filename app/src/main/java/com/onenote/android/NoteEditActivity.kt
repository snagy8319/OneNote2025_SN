package com.onenote.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.widget.Button
import android.widget.Toast

class NoteEditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_edit)

        // Set up toolbar
        setSupportActionBar(findViewById(R.id.toolbar))

        val buttonSave = findViewById<Button>(R.id.buttonSave)
        buttonSave.setOnClickListener{
            Toast.makeText(this, "Saved", Toast.LENGTH_LONG).show()
            // TODO Save title and message in Preferences.
            // TODO Close activity and display title and message in NoteListActivity.
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)

        return super.onCreateOptionsMenu(menu)
    }
}
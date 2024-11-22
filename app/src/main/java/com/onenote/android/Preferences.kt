package com.onenote.android

import android.content.Context

class Preferences(context: Context) {

    private val preferences = context.getSharedPreferences("preferences_note", Context.MODE_PRIVATE)

    fun setNoteTitle(noteTitle: String?) {
        preferences.edit().putString("note_title", noteTitle).apply()
    }

    fun getNoteTitle(): String? {
        return preferences.getString("note_title", null)
    }

    fun setNoteMessage(noteMessage: String?) {
        preferences.edit().putString("note_message", noteMessage).apply()
    }

    fun getNoteMessage(): String? {
        return preferences.getString("note_message", null)
    }
}
package com.onenote.android

import android.content.Context

class Preferences(context: Context) {

    companion object {
        const val NOTE_TITLE = "note_title"
        const val NOTE_MESSAGE = "note_message"
        const val PREFERENCES_NAME = "preferences_note"
    }

    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun setNoteTitle(noteTitle: String?) {
        preferences.edit().putString(NOTE_TITLE, noteTitle).apply()
    }

    fun getNoteTitle(): String? {
        return preferences.getString(NOTE_TITLE, null)
    }

    fun setNoteMessage(noteMessage: String?) {
        preferences.edit().putString(NOTE_MESSAGE, noteMessage).apply()
    }

    fun getNoteMessage(): String? {
        return preferences.getString(NOTE_MESSAGE, null)
    }
}
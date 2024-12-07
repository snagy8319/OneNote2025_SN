package com.onenote.android

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

/**
 * Adapter class for displaying notes in a ListView.
 * @param context The context in which the adapter is running.
 * @param notes The list of notes to be displayed.
 * @param selectedNoteId The ID of the currently selected note.
 */
class NoteAdapter(context: Context, var notes: List<Note>, var selectedNoteId: Int): BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    /**
     * Returns the number of notes.
     * @return The size of the notes list.
     */
    override fun getCount(): Int {
        return notes.size
    }

    /**
     * Returns the note at the specified position.
     * @param position The position of the note in the list.
     * @return The note at the specified position.
     */
    override fun getItem(position: Int): Any {
        return notes[position]
    }

    /**
     * Returns the ID of the note at the specified position.
     * @param position The position of the note in the list.
     * @return The ID of the note at the specified position.
     */
    override fun getItemId(position: Int): Long {
        return notes[position].id.toLong()
    }

    /**
     * Returns the view for the note at the specified position.
     * @param position The position of the note in the list.
     * @param convertView The old view to reuse, if possible.
     * @param parent The parent that this view will eventually be attached to.
     * @return The view for the note at the specified position.
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = inflater.inflate(R.layout.list_item_view, parent, false)

            holder = ViewHolder()
            holder.title = view.findViewById(R.id.title)
            holder.message = view.findViewById(R.id.message)

            view.tag = holder
        } else {
            view = convertView
            holder = convertView.tag as ViewHolder
        }

        val note = notes[position]
        holder.title.text = note.title
        holder.message.text = note.message

        // Highlight the selected item
        if (note.id == selectedNoteId) {
            view.setBackgroundColor(Color.rgb(249, 249, 249))
        } else {
            view.setBackgroundColor(Color.TRANSPARENT)
        }

        return view
    }


    /**
     * ViewHolder class to hold the views for each note item.
     */
    private class ViewHolder {
        lateinit var title: TextView
        lateinit var message: TextView
    }
}
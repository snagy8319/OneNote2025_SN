package com.onenote.android

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class NoteAdapter(context: Context, var notes: List<Note>, var selectedNoteId: Int): BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return notes.size
    }

    override fun getItem(position: Int): Any {
        return notes[position]
    }

    override fun getItemId(position: Int): Long {
        return notes[position].id.toLong()
    }

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

    private class ViewHolder {
        lateinit var title: TextView
        lateinit var message: TextView
    }
}
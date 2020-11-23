package ru.studymushrooms.ui.notes

import android.widget.TextView
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import ru.studymushrooms.R
import ru.studymushrooms.db.Note

class NoteItem(val noteModel: Note) : Item<GroupieViewHolder>() {
    override fun getLayout(): Int = R.layout.note_item

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.note_title_textview).text = noteModel.title
        viewHolder.itemView.findViewById<TextView>(R.id.note_content_textview).text =
            noteModel.content
        viewHolder.itemView.findViewById<TextView>(R.id.note_date_textview).text =
            noteModel.date.toString()
    }
}
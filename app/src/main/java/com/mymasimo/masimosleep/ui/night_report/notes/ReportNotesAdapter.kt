package com.mymasimo.masimosleep.ui.night_report.notes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mymasimo.masimosleep.databinding.NoteRowBinding
import java.text.SimpleDateFormat
import java.util.*

class ReportNotesAdapter(
    private val notes : MutableList<Note>
) : RecyclerView.Adapter<NoteViewHolder>() {

    override fun getItemCount(): Int = notes.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val bindings = NoteRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(bindings)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bindData(notes[position])
    }

    fun setNotes(notes: List<Note>) {
        this.notes.clear()
        this.notes.addAll(notes)
        notifyDataSetChanged()
    }
}

class NoteViewHolder(
    private val bindings: NoteRowBinding
) : RecyclerView.ViewHolder(bindings.root) {
    fun bindData(note: Note) {
        val formatter = SimpleDateFormat("h:mm aa", Locale.getDefault())
        val startTimeCalendar = Calendar.getInstance().apply {
            timeInMillis = note.createdAt
        }

        bindings.timeLabel.text = formatter.format(startTimeCalendar.time)
        bindings.noteText.text = note.note
    }
}

package by.bsuir.notesapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView

class NotesAdapter(
    private var notes: List<Note>,
    context: Context,
    private var onDelete: () -> Unit,
) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    private val db: DatabaseHelper = DatabaseHelper(context)

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val dateTimeTextView: TextView = itemView.findViewById(R.id.dateTimeTextView)
        val updateButton: ImageView = itemView.findViewById(R.id.updateButton)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
        val changeLabelButton: ImageView = itemView.findViewById(R.id.changeLabelButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val lifeCycleOwner = parent.context as LifecycleOwner
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)
        return NoteViewHolder(view)
    }

    override fun getItemCount(): Int = notes.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]


        var label = ""
        if (note.label != null) {
            label = "[" + db.getLabelById(note.label.id)?.name + "] "
        }

        holder.titleTextView.text = label + note.title
        holder.descriptionTextView.text = note.description
        holder.dateTimeTextView.text = note.dateTime

        holder.updateButton.setOnClickListener {
            val intent = Intent(holder.itemView.context, UpdateNoteActivity::class.java).apply {
                putExtra("note_id", note.id)
            }
            holder.itemView.context.startActivity(intent)
        }

        holder.deleteButton.setOnClickListener {
            db.deleteNote(note.id)
            refreshData(db.getAllNotes())
            val context = holder.itemView.context
            ToastProxy.instance.showToast(context, context.getString(R.string.toast_deleted_note))
            onDelete()
        }

        holder.changeLabelButton.setOnClickListener {
            showLabelMenu(holder, note)
        }
    }

    private fun showLabelMenu(holder: NoteViewHolder, note: Note) {
        val popupMenu = PopupMenu(holder.itemView.context, holder.changeLabelButton)
        val labels = db.getAllLabels()

        labels.forEach { label ->
            popupMenu.menu.add(label.name)
        }

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            val selectedLabel = labels.find { it.name == item.title }
            selectedLabel?.let {
                db.updateNoteLabel(note.id, it.id)
                refreshData(db.getAllNotes())
            }
            true
        }

        popupMenu.show()
    }

    fun refreshData(newNotes: List<Note>) {
        notes = newNotes
        notifyDataSetChanged()
    }
}

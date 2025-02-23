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
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView

class NotesAdapter(
    private var notes: List<Note>,
    context: Context,
    private var onDelete: () -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val db: DatabaseHelper = DatabaseHelper(context)

    private val NORMAL_VIEW_TYPE = 0
    private val FOOTER_VIEW_TYPE = 1
    private val FOOTER_COUNT = 1 // Количество пустых элементов в конце списка

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val dateTimeTextView: TextView = itemView.findViewById(R.id.dateTimeTextView)
        val updateButton: ImageView = itemView.findViewById(R.id.updateButton)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
        val changeLabelButton: ImageView = itemView.findViewById(R.id.changeLabelButton)
        val deleteLabelButton: ImageView = itemView.findViewById(R.id.deleteLabelButton)
    }

    class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun getItemViewType(position: Int): Int {
        return if (position >= notes.size) FOOTER_VIEW_TYPE else NORMAL_VIEW_TYPE
    }

    override fun getItemCount(): Int = notes.size + FOOTER_COUNT

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == FOOTER_VIEW_TYPE) {
            val view = View(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    350 // Высота пустого элемента
                )
            }
            FooterViewHolder(view)
        } else {
            val lifeCycleOwner = parent.context as LifecycleOwner
            val view = LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)
            NoteViewHolder(view)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is NoteViewHolder && position < notes.size) {
            val note = notes[position]

            var label = ""
            if (note.label != null) {
                label = "[" + db.getLabelById(note.label!!.id)?.name + "] "
            }

            holder.titleTextView.text = label + note.title
            holder.descriptionTextView.text = note.description
            holder.dateTimeTextView.text = note.dateTime

            if (note.label != null) {
                holder.changeLabelButton.visibility = View.GONE
                holder.deleteLabelButton.visibility = View.VISIBLE
            } else {
                holder.changeLabelButton.visibility = View.VISIBLE
                holder.deleteLabelButton.visibility = View.GONE
            }

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
                Toast.makeText(context, context.getString(R.string.toast_deleted_note), Toast.LENGTH_SHORT).show()
                onDelete()
            }

            holder.changeLabelButton.setOnClickListener {
                showLabelMenu(holder, note)
            }

            holder.deleteLabelButton.setOnClickListener {
                note.label = null
                db.updateNote(note)
                refreshData(db.getAllNotes())
            }
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

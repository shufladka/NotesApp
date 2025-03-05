package by.bsuir.notesapp.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import by.bsuir.notesapp.R
import by.bsuir.notesapp.activity.AddLabelActivity
import by.bsuir.notesapp.activity.UpdateNoteActivity
import by.bsuir.notesapp.database.DatabaseHelper
import by.bsuir.notesapp.entity.Note

class NotesAdapter(
    private var notes: List<Note>,
    context: Context,
    private var onDelete: () -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val databaseHelper: DatabaseHelper = DatabaseHelper(context)
    private var activity: AppCompatActivity = context as AppCompatActivity
    private var noteForLabelChange: Note? = null

    private val normalViewType = 0
    private val footerViewType = 1

    // Количество пустых элементов в конце списка
    private val emptyItems = 1

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
        return if (position >= notes.size) footerViewType else normalViewType
    }

    override fun getItemCount(): Int = notes.size + emptyItems

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == footerViewType) {
            val view = View(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    350
                )
            }
            FooterViewHolder(view)
        } else {
            parent.context as LifecycleOwner
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
                label = "[" + databaseHelper.getLabelById(note.label!!.id)?.name + "] "
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
                databaseHelper.deleteNote(note.id)
                refreshData(databaseHelper.getAllNotes())
                val context = holder.itemView.context
                Toast.makeText(context, context.getString(R.string.toast_deleted_note), Toast.LENGTH_SHORT).show()
                onDelete()
            }

            holder.changeLabelButton.setOnClickListener {
                noteForLabelChange = note
                val intent = Intent(holder.itemView.context, AddLabelActivity::class.java)
                addLabelLauncher.launch(intent)

            }

            holder.deleteLabelButton.setOnClickListener {
                databaseHelper.deleteLabel(note.label!!.id)
                note.label = null
                databaseHelper.updateNote(note)
                refreshData(databaseHelper.getAllNotes())
            }
        }
    }

    private val addLabelLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val labelId = result.data?.getIntExtra("label_id", -1) ?: -1
            noteForLabelChange?.let { note ->
                if (labelId != -1) {
                    val newLabel = databaseHelper.getLabelById(labelId)

                    if (newLabel != null) {
                        note.label = newLabel
                        databaseHelper.updateNote(note)
                        databaseHelper.getNoteById(note.id)
                        refreshData(databaseHelper.getAllNotes())
                    }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshData(newNotes: List<Note>) {
        notes = newNotes
        notifyDataSetChanged()
    }
}

package by.bsuir.notesapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import by.bsuir.notesapp.databinding.ActivityUpdateNoteBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class UpdateNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateNoteBinding
    private lateinit var db: DatabaseHelper
    private var noteId: Int = -1

    @SuppressLint("VisibleForTests")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)

        noteId = intent.getIntExtra("note_id", -1)
        if (noteId == -1) {
            finish()
            return
        }

        val note = db.getNoteById(noteId)

        binding.updateTitleEditText.setText(note?.title)
        binding.updateContentEditText.setText(note?.description)

        binding.updateSaveButton.setOnClickListener {
            val title = binding.updateTitleEditText.text.toString()
            val description = binding.updateContentEditText.text.toString()

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            val dateTime = LocalDateTime.now().format(formatter).toString()

            val updateNote = Note(noteId, title, description, dateTime)
            db.updateNote(updateNote)
            finish()
            ToastProxy.instance.showToast(this, getString(R.string.toast_changes_note))
        }
    }
}

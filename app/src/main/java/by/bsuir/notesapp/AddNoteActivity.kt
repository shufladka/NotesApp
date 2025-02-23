package by.bsuir.notesapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import by.bsuir.notesapp.databinding.ActivityAddNoteBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AddNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNoteBinding
    private lateinit var db: DatabaseHelper

    @SuppressLint("VisibleForTests")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)

        binding.saveButton.setOnClickListener {
            val title = binding.titleEditText.text.toString()
            val description = binding.descriptionEditText.text.toString()

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            val dateTime = LocalDateTime.now().format(formatter).toString()

            if (title.isBlank() || description.isBlank()) {
                ToastProxy.instance.showToast(this, getString(R.string.toast_error_creation_note))
                return@setOnClickListener
            }
//
//            val label1 = Label(0, "TEST1", 1)
//            db.insertLabel(label1)
//            val label2 = Label(1, "TEST2", 2)
//            db.insertLabel(label2)

            val note = Note(0, title, description, dateTime)
            db.insertNote(note)
            finish()
            ToastProxy.instance.showToast(this, getString(R.string.toast_successful_creation_note))
                .show()
        }
    }
}
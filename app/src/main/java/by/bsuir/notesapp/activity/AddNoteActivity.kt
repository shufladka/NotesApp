package by.bsuir.notesapp.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import by.bsuir.notesapp.database.DatabaseHelper
import by.bsuir.notesapp.entity.Note
import by.bsuir.notesapp.R
import by.bsuir.notesapp.toast.ToastProxy
import by.bsuir.notesapp.databinding.ActivityAddNoteBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AddNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNoteBinding
    private lateinit var databaseHelper: DatabaseHelper

    @SuppressLint("VisibleForTests")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelper(this)

        binding.saveButton.setOnClickListener {
            val title = binding.titleEditText.text.toString()
            val description = binding.descriptionEditText.text.toString()

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            val dateTime = LocalDateTime.now().format(formatter).toString()

            if (title.isBlank() || description.isBlank()) {
                ToastProxy.instance.showToast(this, getString(R.string.toast_error_creation_note))
                return@setOnClickListener
            }

            val note = Note(0, title, description, dateTime)
            databaseHelper.insertNote(note)
            finish()
        }
    }
}
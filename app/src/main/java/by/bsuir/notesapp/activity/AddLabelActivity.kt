package by.bsuir.notesapp.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import by.bsuir.notesapp.database.DatabaseHelper
import by.bsuir.notesapp.entity.Label
import by.bsuir.notesapp.R
import by.bsuir.notesapp.toast.ToastProxy
import by.bsuir.notesapp.databinding.ActivityAddLabelBinding

class AddLabelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddLabelBinding
    private lateinit var databaseHelper: DatabaseHelper

    @SuppressLint("VisibleForTests")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddLabelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelper(this)

        binding.saveButton.setOnClickListener {
            val name = binding.nameText.text.toString()
            val priority = binding.priorityInt.text.toString()

            if (title.isBlank() || priority.isBlank() ) {
                ToastProxy.instance.showToast(this, getString(R.string.toast_error_creation_label))
                return@setOnClickListener
            }

            val labelId = databaseHelper.insertLabel(Label(0, name, priority.toInt()))

            // Проверяем, что метка действительно добавлена
            if (labelId != -1L) {
                val resultIntent = Intent().apply {
                    putExtra("label_id", labelId.toInt())
                }
                setResult(RESULT_OK, resultIntent)
            } else {
                setResult(RESULT_CANCELED)
            }
            finish()
        }
    }
}
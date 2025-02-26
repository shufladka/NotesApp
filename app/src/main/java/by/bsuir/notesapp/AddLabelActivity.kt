package by.bsuir.notesapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import by.bsuir.notesapp.databinding.ActivityAddLabelBinding

class AddLabelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddLabelBinding
    private lateinit var db: DatabaseHelper

    @SuppressLint("VisibleForTests")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddLabelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)

        binding.saveButton.setOnClickListener {
            val name = binding.nameText.text.toString()
            val priority = binding.priorityInt.text.toString()

            if (title.isBlank() || priority.isBlank() ) {
                ToastProxy.instance.showToast(this, getString(R.string.toast_error_creation_label))
                return@setOnClickListener
            }

            val labelId = db.insertLabel(Label(0, name, priority.toInt()))

            if (labelId != -1L) { // Проверяем, что метка действительно добавлена
                val resultIntent = Intent().apply {
                    putExtra("label_id", labelId.toInt()) // Приводим к Int
                }
                setResult(RESULT_OK, resultIntent)
            } else {
                ToastProxy.instance.showToast(this, "Ошибка при сохранении метки").show()
                setResult(RESULT_CANCELED)
            }
            finish()
            ToastProxy.instance.showToast(this, labelId.toString())
                .show()
        }
    }
}
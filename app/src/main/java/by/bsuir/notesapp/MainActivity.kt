package by.bsuir.notesapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.view.Menu
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import by.bsuir.notesapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: DatabaseHelper
    private lateinit var notesAdapter: NotesAdapter
    private var selectedCheckBox: CheckBox? = null // Хранит активный чекбокс
    private var isMenuVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)

        notesAdapter =
            NotesAdapter(db.getAllNotes(), this, onDelete = { updateUI(db.getAllNotes()) })

        // Устанавливаем сортировку по умолчанию
        setActiveSort(binding.disableSortCheck)
        updateNotesList()

        // Обработчики нажатий на контейнеры (чтобы работало нажатие не только по тексту, но и по всей строке)
        binding.disableSortContainer.setOnClickListener {
            setActiveSort(binding.disableSortCheck)
            updateNotesList()
        }

        binding.directSortContainer.setOnClickListener {
            setActiveSort(binding.directSortCheck)
            updateNotesList { notes ->
                notes.sortedBy { it.label?.priority ?: Int.MAX_VALUE }
            }
        }

        binding.reverseSortContainer.setOnClickListener {
            setActiveSort(binding.reverseSortCheck)
            updateNotesList { notes ->
                notes.sortedByDescending { it.label?.priority ?: Int.MIN_VALUE }
            }
        }

        binding.notesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.notesRecyclerView.adapter = notesAdapter

        binding.sortButton.setOnClickListener {
            toggleMenu()
        }

        binding.addButton.setOnClickListener {
            startActivity(Intent(this, AddNoteActivity::class.java))
        }
    }

    private fun toggleMenu() {
        if (isMenuVisible) {
            binding.dropdownMenu.visibility = View.GONE
        } else {
            binding.dropdownMenu.visibility = View.VISIBLE
        }
        isMenuVisible = !isMenuVisible
    }

    // Функция обновления списка заметок
    private fun updateNotesList(sortFunction: ((List<Note>) -> List<Note>)? = null) {
        var notes = db.getAllNotes()
        notes = sortFunction?.invoke(notes) ?: notes
        notesAdapter.refreshData(notes)
        updateUI(notes)
    }

    // Устанавливаем активную сортировку
    private fun setActiveSort(checkBox: CheckBox) {
        selectedCheckBox?.isChecked = false // Сбрасываем предыдущий чекбокс
        checkBox.isChecked = true // Выбираем новый
        selectedCheckBox = checkBox
    }

    private fun updateUI(note: List<Note>?) {
        if (note != null) {
            binding.notesRecyclerView.visibility = if (note.isNotEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        val note = db.getAllNotes()
        notesAdapter.refreshData(note)
        updateUI(note)
    }
}

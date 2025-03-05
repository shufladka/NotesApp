package by.bsuir.notesapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.View
import android.widget.CheckBox
import androidx.recyclerview.widget.LinearLayoutManager
import by.bsuir.notesapp.activity.AddNoteActivity
import by.bsuir.notesapp.adapter.NotesAdapter
import by.bsuir.notesapp.database.DatabaseHelper
import by.bsuir.notesapp.databinding.ActivityMainBinding
import by.bsuir.notesapp.entity.Note

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var notesAdapter: NotesAdapter
    private var selectedCheckBox: CheckBox? = null
    private var isMenuVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelper(this)

        notesAdapter = NotesAdapter(
            databaseHelper.getAllNotes(),
            this,
            onDelete = { updateUI(databaseHelper.getAllNotes()) })

        // Устанавливаем сортировку по умолчанию
        setActiveSort(binding.disableSortCheck)
        updateNotesList()

        // Обработчики нажатий на контейнеры
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

    // Обновление списка заметок
    private fun updateNotesList(sortFunction: ((List<Note>) -> List<Note>)? = null) {
        var notes = databaseHelper.getAllNotes()
        notes = sortFunction?.invoke(notes) ?: notes
        notesAdapter.refreshData(notes)
        updateUI(notes)
    }

    // Устанавка активной сортировки
    private fun setActiveSort(checkBox: CheckBox) {

        // Сбрасываем предыдущий чекбокс
        selectedCheckBox?.isChecked = false

        // Выбираем новый чекбокс
        checkBox.isChecked = true
        selectedCheckBox = checkBox
    }

    private fun updateUI(note: List<Note>?) {
        if (note != null) {
            binding.notesRecyclerView.visibility = if (note.isNotEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        val note = databaseHelper.getAllNotes()
        notesAdapter.refreshData(note)
        updateUI(note)
    }
}

package by.bsuir.notesapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.Menu
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import by.bsuir.notesapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: DatabaseHelper
    private lateinit var notesAdapter: NotesAdapter
    private var isMenuVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)

        notesAdapter = NotesAdapter(db.getAllNotes(), this, onDelete = { updateUI(db.getAllNotes()) })

        binding.notesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.notesRecyclerView.adapter = notesAdapter

        binding.addButton.setOnClickListener {
            startActivity(Intent(this, AddNoteActivity::class.java))
        }
//        binding.addButton.setOnClickListener {
//            toggleMenu()
//        }
//
//        binding.createNote.setOnClickListener {
//            startActivity(Intent(this, AddNoteActivity::class.java))
//            toggleMenu()
//        }
//
//        binding.createLabel.setOnClickListener {
//            startActivity(Intent(this, AddLabelActivity::class.java))
//            toggleMenu()
//        }
    }

//    private fun toggleMenu() {
//        if (isMenuVisible) {
//            binding.dropdownMenu.visibility = View.GONE
//        } else {
//            binding.dropdownMenu.visibility = View.VISIBLE
//        }
//        isMenuVisible = !isMenuVisible
//    }

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

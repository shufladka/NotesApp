package by.bsuir.notesapp.entity

data class Note(val id: Int, val title: String, val description: String, val dateTime: String, var label: Label? = null)

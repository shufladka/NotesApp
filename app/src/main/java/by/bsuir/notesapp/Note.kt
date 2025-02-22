package by.bsuir.notesapp

data class Note(val id: Int, val title: String, val description: String, val dateTime: String, val label: Label? = null)

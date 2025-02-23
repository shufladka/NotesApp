package by.bsuir.notesapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, SCHEMA) {

    companion object {
        private const val DATABASE_NAME = "notesapp.db"
        private const val SCHEMA = 1

        // Таблица для заметок
        private const val TABLE_NOTES = "notes"
        private const val COLUMN_NOTE_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_DESCRIPTION = "description"
        private const val COLUMN_DATETIME = "datetime"
        private const val COLUMN_LABEL_ID = "label_id" // Внешний ключ на таблицу labels

        // Таблица для текстовых меток
        private const val TABLE_LABELS = "labels"
        private const val COLUMN_LABEL_NAME = "name"
        private const val COLUMN_LABEL_PRIORITY = "priority"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Создаем таблицу labels
        val createLabelsTable = """
            CREATE TABLE $TABLE_LABELS (
                $COLUMN_NOTE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_LABEL_NAME TEXT,
                $COLUMN_LABEL_PRIORITY INTEGER
            )
        """.trimIndent()

        // Создаем таблицу notes с внешним ключом на labels
        val createNotesTable = """
            CREATE TABLE $TABLE_NOTES (
                $COLUMN_NOTE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT,
                $COLUMN_DESCRIPTION TEXT,
                $COLUMN_DATETIME TEXT,
                $COLUMN_LABEL_ID INTEGER,
                FOREIGN KEY ($COLUMN_LABEL_ID) REFERENCES $TABLE_LABELS ($COLUMN_NOTE_ID) ON DELETE SET NULL
            )
        """.trimIndent()

        db?.execSQL(createLabelsTable)
        db?.execSQL(createNotesTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NOTES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_LABELS")
        onCreate(db)
    }

    /**
     * Вставка метки
     */
    fun insertLabel(label: Label): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_LABEL_NAME, label.name)
            put(COLUMN_LABEL_PRIORITY, label.priority)
        }
        val id = db.insert(TABLE_LABELS, null, values)
        db.close()
        return id
    }

    /**
     * Получение всех меток
     */
    fun getAllLabels(): List<Label> {
        val labels = mutableListOf<Label>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_LABELS", null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTE_ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LABEL_NAME))
            val priority = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LABEL_PRIORITY))

            labels.add(Label(id, name, priority))
        }
        cursor.close()
        db.close()
        return labels
    }

    /**
     * Получение метки по идентификатору
     */
    fun getLabelById(labelId: Int): Label? {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_LABELS WHERE $COLUMN_NOTE_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(labelId.toString()))
        var label: Label? = null

        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTE_ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LABEL_NAME))
            val priority = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LABEL_PRIORITY))

            label = Label(id, name, priority)
        }
        cursor.close()
        db.close()
        return label
    }


    /**
     * Вставка заметки
     */
    fun insertNote(note: Note): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, note.title)
            put(COLUMN_DESCRIPTION, note.description)
            put(COLUMN_DATETIME, note.dateTime)
            put(COLUMN_LABEL_ID, note.label?.id) // Записываем id метки, если есть
        }
        val id = db.insert(TABLE_NOTES, null, values)
        db.close()
        return id
    }

    /**
     * Получение всех заметок с метками (если есть)
     */
    fun getAllNotes(): List<Note> {
        val notes = mutableListOf<Note>()
        val db = readableDatabase
        val query = """
            SELECT n.$COLUMN_NOTE_ID, n.$COLUMN_TITLE, n.$COLUMN_DESCRIPTION, n.$COLUMN_DATETIME,
                   l.$COLUMN_NOTE_ID AS label_id, l.$COLUMN_LABEL_NAME, l.$COLUMN_LABEL_PRIORITY
            FROM $TABLE_NOTES AS n
            LEFT JOIN $TABLE_LABELS AS l ON n.$COLUMN_LABEL_ID = l.$COLUMN_NOTE_ID
        """.trimIndent()

        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTE_ID))
            val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
            val description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION))
            val dateTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATETIME))

            val labelIdIndex = cursor.getColumnIndex("label_id")
            val label = if (labelIdIndex != -1 && !cursor.isNull(labelIdIndex)) {
                Label(
                    id = cursor.getInt(labelIdIndex),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LABEL_NAME)),
                    priority = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LABEL_PRIORITY))
                )
            } else {
                null
            }

            notes.add(Note(id, title, description, dateTime, label))
        }
        cursor.close()
        db.close()
        return notes
    }

    /**
     * Получение заметки по идентификатору
     */
    fun getNoteById(noteId: Int): Note? {
        val db = readableDatabase
        val query = """
        SELECT n.$COLUMN_NOTE_ID, n.$COLUMN_TITLE, n.$COLUMN_DESCRIPTION, n.$COLUMN_DATETIME,
               l.$COLUMN_NOTE_ID AS label_id, l.$COLUMN_LABEL_NAME, l.$COLUMN_LABEL_PRIORITY
        FROM $TABLE_NOTES AS n
        LEFT JOIN $TABLE_LABELS AS l ON n.$COLUMN_LABEL_ID = l.$COLUMN_NOTE_ID
        WHERE n.$COLUMN_NOTE_ID = ?
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(noteId.toString()))
        var note: Note? = null

        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTE_ID))
            val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
            val description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION))
            val dateTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATETIME))

            val labelIdIndex = cursor.getColumnIndex("label_id")
            val label = if (labelIdIndex != -1 && !cursor.isNull(labelIdIndex)) {
                Label(
                    id = cursor.getInt(labelIdIndex),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LABEL_NAME)),
                    priority = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LABEL_PRIORITY))
                )
            } else {
                null
            }

            note = Note(id, title, description, dateTime, label)
        }
        cursor.close()
        db.close()
        return note
    }


    /**
     * Обновление заметки
     */
    fun updateNote(note: Note): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, note.title)
            put(COLUMN_DESCRIPTION, note.description)
            put(COLUMN_DATETIME, note.dateTime)
            put(COLUMN_LABEL_ID, note.label?.id)
        }
        val affectedRows = db.update(TABLE_NOTES, values, "$COLUMN_NOTE_ID = ?", arrayOf(note.id.toString()))
        db.close()
        return affectedRows
    }

    /**
     * Добавление новой метки к существующей заметке
     */
    fun updateNoteLabel(noteId: Int, labelId: Int?) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("label_id", labelId)
        }
        db.update("notes", values, "id=?", arrayOf(noteId.toString()))
        db.close()
    }

    /**
     * Удаление заметки
     */
    fun deleteNote(noteId: Int) {
        val db = writableDatabase
        db.delete(TABLE_NOTES, "$COLUMN_NOTE_ID = ?", arrayOf(noteId.toString()))
        db.close()
    }

    /**
     * Удаление метки (если удаляется метка, у заметок она становится NULL)
     */
    fun deleteLabel(labelId: Int) {
        val db = writableDatabase
        db.delete(TABLE_LABELS, "$COLUMN_NOTE_ID = ?", arrayOf(labelId.toString()))
        db.close()
    }
}

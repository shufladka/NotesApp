package by.bsuir.notesapp

import android.content.ContentValues

abstract class BaseEntity {
    abstract fun toContentValues(): ContentValues
}
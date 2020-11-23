package ru.studymushrooms.db

import android.database.Cursor
import androidx.room.*
import org.threeten.bp.OffsetDateTime

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val uid: Int? = null,
    @ColumnInfo(name = "content") val content: String? = null,
    @TypeConverters(TiviTypeConverters::class) @ColumnInfo(name = "date") val date: OffsetDateTime? = null,
    @ColumnInfo(name = "title") val title: String? = null
)

@Dao
interface NotesDao {
    @Query("SELECT * FROM notes ORDER BY date")
    fun getAll(): List<Note>

    @Query("SELECT * FROM notes WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<Note>

    @Query("SELECT * FROM notes WHERE title LIKE (:title)")
    fun findByTitle(title: String): Cursor

    @Insert
    fun insertAll(vararg notes: Note)

    @Delete
    fun delete(note: Note)
}
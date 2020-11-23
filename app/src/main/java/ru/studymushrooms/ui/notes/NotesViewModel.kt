package ru.studymushrooms.ui.notes

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import okhttp3.ResponseBody
import org.threeten.bp.OffsetDateTime
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.studymushrooms.App
import ru.studymushrooms.db.Note
import java.util.*

class NotesViewModel : ViewModel() {

    val notes: MutableLiveData<List<Note>> = MutableLiveData()
    private val note: MutableLiveData<Note> = MutableLiveData()

    fun loadData(context: Context) {
        val notes = App.db.notesDao().getAll().asReversed()
        this.notes.postValue(notes)
    }

    fun saveNote(title: String, content: String) {
        val n = Note(
            title = title,
            content = content,
            date = OffsetDateTime.now(),
        )

        note.postValue(
            n
        )
    }
}

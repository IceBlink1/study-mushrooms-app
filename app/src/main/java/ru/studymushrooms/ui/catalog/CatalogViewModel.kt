package ru.studymushrooms.ui.catalog

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.studymushrooms.App
import ru.studymushrooms.db.Mushroom

class CatalogViewModel : ViewModel() {
    val mushrooms: MutableLiveData<List<Mushroom>> = MutableLiveData()

    fun loadData(context: Context) {
        if (mushrooms.value == null) {
            val allMushrooms = App.db.mushroomDao().getAll()
            mushrooms.postValue(allMushrooms)
        }
    }
}
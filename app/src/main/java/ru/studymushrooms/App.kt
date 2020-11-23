package ru.studymushrooms

import android.app.Application
import androidx.room.Room
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.studymushrooms.db.AppDatabase

class App : Application() {


    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "db.db")
            .createFromAsset("db.db")
            .allowMainThreadQueries().build()
    }

    companion object {
        lateinit var db: AppDatabase
    }

}

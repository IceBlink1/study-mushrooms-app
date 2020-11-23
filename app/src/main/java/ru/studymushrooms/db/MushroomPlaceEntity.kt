package ru.studymushrooms.db

import androidx.room.*
import org.threeten.bp.OffsetDateTime

@Entity(tableName = "mushroomPlaces")
data class MushroomPlace(
    @PrimaryKey(autoGenerate = true) val uid: Int? = null,
    @ColumnInfo(name = "picture") val pictureUri: String? = null,
    @ColumnInfo(name = "longitude") val longitude: Double? = null,
    @ColumnInfo(name = "latitude") val latitude: Double? = null,
    @ColumnInfo(name = "date") @TypeConverters(TiviTypeConverters::class) val pictureDate: OffsetDateTime? = null
)

@Dao
interface MushroomPlaceDao {
    @Query("SELECT * FROM mushroomplaces")
    fun getAll(): List<MushroomPlace>

    @Query("SELECT * FROM mushroomplaces WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<MushroomPlace>

    @Insert
    fun insertAll(vararg mushroomsPlace: MushroomPlace)

    @Delete
    fun delete(mushroomPlace: MushroomPlace)
}

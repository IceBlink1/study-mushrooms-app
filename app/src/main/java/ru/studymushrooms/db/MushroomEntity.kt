package ru.studymushrooms.db

import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.room.*

@Entity(
    tableName = "mushrooms",
    indices = [Index(value = ["classname", "name", "description", "id", "type", "picture"])]
)
data class Mushroom(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int?,
    @ColumnInfo(name = "classname") val classname: String?,
    @ColumnInfo(name = "picture") val pictureLink: String?,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "type") val type: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id!!)
        parcel.writeString(classname)
        parcel.writeString(pictureLink)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeString(type)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Mushroom> {
        override fun createFromParcel(parcel: Parcel): Mushroom {
            return Mushroom(parcel)
        }

        override fun newArray(size: Int): Array<Mushroom?> {
            return arrayOfNulls(size)
        }
    }
}

@Dao
interface MushroomDao {
    @Query("SELECT * FROM mushrooms")
    fun getAll(): List<Mushroom>

    @Query("SELECT * FROM mushrooms WHERE id IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): LiveData<List<Mushroom>>

    @Query("SELECT * FROM mushrooms WHERE classname LIKE (:name)")
    fun findByClassname(name: String): List<Mushroom>

    @Insert
    fun insertAll(vararg mushrooms: Mushroom)

    @Delete
    fun delete(mushroom: Mushroom)
}

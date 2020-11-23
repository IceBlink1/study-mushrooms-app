package ru.studymushrooms.ui.maps

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.studymushrooms.App

class MapsViewModel : ViewModel() {

    fun getPlaces(map: MapView, resources: Resources) {
        for (i in map.overlays)
            if (i is Marker)
                map.overlays.remove(i)
        val places = App.db.mushroomPlaceDao().getAll()
        for (i in places) {
            val m = object : Target, Marker(map) {
                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                    image = placeHolderDrawable
                }

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                    image = errorDrawable
                }

                override fun onBitmapLoaded(
                    bitmap: Bitmap?,
                    from: Picasso.LoadedFrom?
                ) {
                    image = BitmapDrawable(resources, bitmap)
                }

            }

            Picasso.get().load(Uri.parse(i.pictureUri?.dropLast(1))).into(m)

            m.position =
                GeoPoint(
                    i.latitude!!,
                    i.longitude!!
                )
            m.title = "Найден " + i.pictureDate!!.toLocalDateTime()

            map.overlays.add(m)
        }
        map.invalidate()

    }

}
package ru.studymushrooms.ui.maps


import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import ru.studymushrooms.R

class MapsFragment : Fragment() {

    private val PREFS_NAME = "ru.studymushrooms.prefs"
    private val PREFS_TILE_SOURCE = "tilesource"
    private val PREFS_LATITUDE_STRING = "latitudeString"
    private val PREFS_LONGITUDE_STRING = "longitudeString"
    private val PREFS_ORIENTATION = "orientation"
    private val PREFS_ZOOM_LEVEL_DOUBLE = "zoomLevelDouble"

    private lateinit var mCompassOverlay: CompassOverlay
    private lateinit var mRotationGestureOverlay: RotationGestureOverlay
    private val viewModel: MapsViewModel by activityViewModels()
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private lateinit var mMapView: MapView
    private lateinit var mPrefs: SharedPreferences
    private lateinit var locationOverlay: MyLocationNewOverlay

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_maps, container, false)
        return root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = activity?.applicationContext
        Configuration.getInstance()
            .load(context, PreferenceManager.getDefaultSharedPreferences(context))

        mMapView = view.findViewById(R.id.map_view) as MapView
        mMapView.isTilesScaledToDpi = true;
        mMapView.setTileSource(TileSourceFactory.MAPNIK)

        requestPermissionsIfNecessary(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )

        mMapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);
        mMapView.setMultiTouchControls(true);
        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mMapView);
        locationOverlay.enableMyLocation()
        mMapView.overlays.add(locationOverlay)

        mRotationGestureOverlay = RotationGestureOverlay(mMapView)
        mRotationGestureOverlay.isEnabled = true
        mMapView.setMultiTouchControls(true)
        mMapView.overlays.add(mRotationGestureOverlay)

        mCompassOverlay =
            CompassOverlay(context, InternalCompassOrientationProvider(context), mMapView)
        mCompassOverlay.enableCompass()
        mMapView.overlays.add(mCompassOverlay)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mPrefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val zoomLevel = mPrefs.getFloat(PREFS_ZOOM_LEVEL_DOUBLE, 1f)
        mMapView.controller.setZoom(zoomLevel.toDouble())
        val orientation = mPrefs.getFloat(PREFS_ORIENTATION, 0f)
        mMapView.setMapOrientation(orientation, false)
        val latitudeString = mPrefs.getString(PREFS_LATITUDE_STRING, "1.0")
        val longitudeString = mPrefs.getString(PREFS_LONGITUDE_STRING, "1.0")
        val latitude = java.lang.Double.valueOf(latitudeString!!)
        val longitude = java.lang.Double.valueOf(longitudeString!!)
        mMapView.setExpectedCenter(GeoPoint(latitude, longitude))
//        viewModel.getPlaces(mMapView, resources)

//        val eventsReceiver: MapEventsReceiver = object : MapEventsReceiver {
//            override fun longPressHelper(p: GeoPoint): Boolean {
////                Toast.makeText(
////                    requireActivity().baseContext,
////                    p.latitude.toString() + " - " + p.longitude.toString(),
////                    Toast.LENGTH_LONG
////                ).show()
//                val k = OverlayItem("text", "textsnippet", "blablabla", p)
//                val m = Marker(mMapView)
//                m.position = p
//                m.textLabelBackgroundColor = Color.TRANSPARENT
////                m.icon = resources.getDrawable(
////                    R.drawable.ic_launcher_background,
////                    requireActivity().theme
////                )
//                m.textLabelForegroundColor = Color.RED
//                m.textLabelFontSize = 40
//                m.setTextIcon("text")
//
//                mMapView.overlays
//                    .add(
//                        ItemizedIconOverlay<OverlayItem>(
//                            context,
//                            mutableListOf(k),
//                            object : OnItemGestureListener<OverlayItem> {
//                                override fun onItemLongPress(
//                                    index: Int,
//                                    item: OverlayItem?
//                                ): Boolean {
//                                    return false
//                                }
//
//                                override fun onItemSingleTapUp(
//                                    index: Int,
//                                    item: OverlayItem?
//                                ): Boolean {
//                                    return false
//                                }
//
//                            })
//                    )
//                mMapView.invalidate()
//                return false
//            }
//
//            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
//                return false
//            }
//
//        }
//        mMapView.overlays.add(MapEventsOverlay(eventsReceiver))


    }

    private fun requestPermissionsIfNecessary(permissions: Array<String>) {
        val permissionsToRequest: ArrayList<String> = ArrayList()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(permission)
            }
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissionsToRequest.toArray(arrayOfNulls(0)),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onPause() {
        val edit: SharedPreferences.Editor = mPrefs.edit()
        edit.putString(PREFS_TILE_SOURCE, mMapView.tileProvider.tileSource.name())
        edit.putFloat(PREFS_ORIENTATION, mMapView.mapOrientation)
        edit.putString(
            PREFS_LATITUDE_STRING,
            java.lang.String.valueOf(mMapView.mapCenter.latitude)
        )
        edit.putString(
            PREFS_LONGITUDE_STRING,
            java.lang.String.valueOf(mMapView.mapCenter.longitude)
        )
        edit.putFloat(PREFS_ZOOM_LEVEL_DOUBLE, mMapView.zoomLevelDouble.toFloat())
        edit.apply()
        super.onPause()
        mMapView.onPause()

    }

    override fun onResume() {
        super.onResume()
        val tileSourceName = mPrefs.getString(
            PREFS_TILE_SOURCE,
            TileSourceFactory.DEFAULT_TILE_SOURCE.name()
        )
        try {
            val tileSource: ITileSource = TileSourceFactory.getTileSource(tileSourceName)
            mMapView.setTileSource(tileSource)
        } catch (e: IllegalArgumentException) {
            mMapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
        }
        viewModel.getPlaces(mMapView, resources)
        mMapView.onResume()
    }
}

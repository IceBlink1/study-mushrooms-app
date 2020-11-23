package ru.studymushrooms.ui.recognize

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cocoahero.android.geojson.Point
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.torchvision.TensorImageUtils
import org.threeten.bp.OffsetDateTime
import ru.studymushrooms.App
import ru.studymushrooms.MainActivity
import ru.studymushrooms.R
import ru.studymushrooms.db.Mushroom
import ru.studymushrooms.db.MushroomPlace
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class RecognitionFragment : Fragment() {
    private lateinit var names: List<String>
    private val STORAGE_REQUEST_CODE = 1
    private val CAMERA_REQUEST_CODE = 2
    private lateinit var textView: TextView
    private lateinit var imageView: ImageView
    private val adapter: GroupAdapter<GroupieViewHolder> = GroupAdapter()
    private lateinit var recyclerView: RecyclerView
    private lateinit var module: Module
    private lateinit var currentPath: String
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_recognition, container, false)
        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermissionsIfNecessary(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestPermissionsIfNecessary(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
        }
        view.findViewById<Button>(R.id.recognize_storage_button).setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, STORAGE_REQUEST_CODE)
        }
        view.findViewById<Button>(R.id.recognize_photo_button).setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                null
            }
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    requireContext(),
                    "ru.studymushrooms.provider",
                    it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            }

            if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
            }
        }

        recyclerView = view.findViewById(R.id.recognize_recyclerview)
        textView = view.findViewById(R.id.recognize_textview)
        imageView = view.findViewById(R.id.recognize_imageview)

        module = Module.load(assetFilePath(requireContext(), "nn.pt"))
        names = view.context.resources.getStringArray(R.array.mushroom_names).sorted()
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File =
            requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPath = absolutePath
        }
    }

    private fun galleryAddPic() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(currentPath)
            mediaScanIntent.data = Uri.fromFile(f)
            requireActivity().sendBroadcast(mediaScanIntent)
        }
    }

    private fun assetFilePath(context: Context, assetName: String): String? {
        val file = File(context.getFilesDir(), assetName)
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath()
        }

        context.assets.open(assetName).use { `is` ->
            FileOutputStream(file).use { os ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (`is`.read(buffer).also { read = it } != -1) {
                    os.write(buffer, 0, read)
                }
                os.flush()
            }
            return file.absolutePath
        }

    }

    fun getDegrees(exifRes: String?, exifRef: String?): Double? {
        if (exifRef == null || exifRes == null) return null
        val a = exifRes.split(",").map {
            val k = it.split('/').map { it.toDouble() }
            return@map k[0] / k[1]
        }
        return (a[0] + a[1] / 60 + a[2] / 3600) * if (exifRef in "SW") -1 else 1
    }

    data class RecognitionModel(val prob: Double, val mushroom: Mushroom)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val picturePath: String
        if (resultCode == RESULT_OK) {
            adapter.clear()
            val image: Bitmap
            var exif: ExifInterface? = null
            if (requestCode == CAMERA_REQUEST_CODE) {
                picturePath = currentPath
                galleryAddPic()
            } else {
                val filePathColumn =
                    arrayOf(MediaStore.Images.Media.DATA)

                val cursor: Cursor? = requireActivity().contentResolver.query(
                    data!!.data!!,
                    filePathColumn, null, null, null
                )
                cursor!!.moveToFirst()

                val columnIndex: Int = cursor.getColumnIndex(filePathColumn[0])
                picturePath = cursor.getString(columnIndex)
                cursor.close()
            }

            exif = ExifInterface(picturePath)
            val m = Matrix()
            m.preRotate(exif.rotationDegrees.toFloat())
            val decodedImage = BitmapFactory.decodeFile(picturePath)
            image = Bitmap.createBitmap(
                decodedImage,
                0,
                0,
                decodedImage.width,
                decodedImage.height,
                m,
                true
            )
            val x = getDegrees(
                exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE),
                exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)
            )
            val y = getDegrees(
                exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE),
                exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)
            )

            val exifLocation = if (x == null || y == null) null else Point(
                x,
                y
            )
            val mutableImage = Bitmap.createScaledBitmap(image, 224, 224, false)
            val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
                mutableImage,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
                TensorImageUtils.TORCHVISION_NORM_STD_RGB
            )
            val outputTensor = module.forward(IValue.from(inputTensor)).toTensor()
            val scores = outputTensor.dataAsFloatArray
            val probs = scores.map { softmax(it, scores) }
            val recognized = mutableListOf<RecognitionModel>()
            for (i in probs.indices) {
                if (probs[i] > 0.01) {
                    val mushrooms = App.db.mushroomDao().findByClassname(names[i])
                    if (mushrooms.size > 0) {
                        recognized.add(RecognitionModel(probs[i], mushrooms[0]))
                    }
                }
            }
            recognized.sortBy { -it.prob }

            for (i in recognized) {

                adapter.add(RecognitionItem(i.mushroom, i.prob))
                recyclerView.adapter = adapter
                recyclerView.layoutManager =
                    LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                recyclerView.visibility = View.VISIBLE
                textView.visibility = View.VISIBLE
                imageView.visibility = View.VISIBLE
                imageView.setImageDrawable(BitmapDrawable(resources, image))

            }
            var useLocation = true
            var alert = MaterialAlertDialogBuilder(requireContext())
            if (requestCode == STORAGE_REQUEST_CODE) {
                alert.setSingleChoiceItems(
                    arrayOf(
                        "Использовтаь текущую геолокацию",
                        "Использовать данные о геолокации картинки"
                    ), 0
                ) { dialog, which ->
                    useLocation = which != 1
                }
                alert.show()
            }

            alert = requireContext().let {
                MaterialAlertDialogBuilder(it).setTitle("Запомнить место?")
                    .setPositiveButton("Да") { dialog, which ->
                        val location = (requireActivity() as MainActivity).location
                        if (exifLocation != null && !useLocation)
                            App.db.mushroomPlaceDao().insertAll(
                                MushroomPlace(
                                    longitude = exifLocation.position.longitude,
                                    latitude = exifLocation.position.latitude,
                                    pictureUri = picturePath,
                                    pictureDate = OffsetDateTime.now()
                                )
                            )
                        else
                            App.db.mushroomPlaceDao().insertAll(
                                MushroomPlace(
                                    longitude = location?.longitude,
                                    latitude = location?.latitude,
                                    pictureDate = OffsetDateTime.now(),
                                    pictureUri = picturePath
                                )
                            )
                        Toast.makeText(
                            context,
                            "Место добавлено",
                            Toast.LENGTH_LONG
                        ).show()


                    }.setNeutralButton("Отмена") { dialog, which ->
                        dialog.dismiss()
                    }
            }
            alert.show()


        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun softmax(input: Float, neuronValues: FloatArray): Double {
        val total: Double =
            Arrays.stream(neuronValues.map { it.toDouble() }.toDoubleArray()).map { v: Double ->
                Math.exp(v)
            }.sum()
        return Math.exp(input.toDouble()) / total
    }

    private fun requestPermissionsIfNecessary(permissions: Array<String>) {
        val permissionsToRequest: ArrayList<String> = ArrayList()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is not granted
                permissionsToRequest.add(permission)
            }
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissionsToRequest.toArray(arrayOfNulls(0)),
                1
            )
        }
    }
}


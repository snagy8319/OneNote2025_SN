package com.onenote.android

import android.Manifest
import android.app.Activity
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class NoteEditActivity : AppCompatActivity() {

    private lateinit var noteDao: NoteDao
    private lateinit var noteEditTitle: EditText
    private lateinit var noteEditMessage: EditText
    private lateinit var buttonSave: Button
    private lateinit var buttonCamera: Button
    private lateinit var buttonGallery: Button
    private lateinit var imagePreview: ImageView
    private lateinit var latitudeTextView: TextView
    private lateinit var longitudeTextView: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var map: MapView

    private var noteId: Int = -1
    private var currentPhotoPath: String? = null
    private var note: Note? = null


    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val configuration = Configuration.getInstance()
        configuration.userAgentValue = BuildConfig.APPLICATION_ID
        configuration.load(this, PreferenceManager.getDefaultSharedPreferences(this))
        setContentView(R.layout.activity_note_edit)

        // Set up toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)

        // Initialize Room DB
        val db = Room.databaseBuilder(
            applicationContext,
            NotesDatabase::class.java, "notes"
        ).allowMainThreadQueries().build()
        noteDao = db.noteDao()

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Find views by ID
        noteEditTitle = findViewById(R.id.noteEditTitle)
        noteEditMessage = findViewById(R.id.noteEditMessage)
        buttonSave = findViewById(R.id.buttonSave)
        buttonCamera = findViewById(R.id.button_camera)
        buttonGallery = findViewById(R.id.button_gallery)
        imagePreview = findViewById(R.id.image_preview)
        latitudeTextView = findViewById(R.id.latitudeTextView)
        longitudeTextView = findViewById(R.id.longitudeTextView)
        map = findViewById(R.id.map)
        map.setMultiTouchControls(true)

        // Check if we are editing an existing note
        noteId = intent.getIntExtra("noteId", -1)

        // Load note if editing
        if (noteId != -1) {
            CoroutineScope(Dispatchers.IO).launch {
                note = noteDao.loadAllByIds(noteId).firstOrNull()
                note?.let {
                    runOnUiThread {
                        noteEditTitle.setText(it.title)
                        noteEditMessage.setText(it.message)
                        it.image?.let { path ->
                            val file = File(path)
                            if (file.exists()) {
                                val uri = Uri.fromFile(file)
                                imagePreview.setImageURI(uri)
                            }
                        }
                        if (it.latitude != null && it.longitude != null) {
                            latitudeTextView.text = "Latitude: ${it.latitude}"
                            longitudeTextView.text = "Longitude: ${it.longitude}"
                            val startPoint = GeoPoint(it.latitude!!, it.longitude!!)
                            map.controller.setZoom(15.0)
                            map.controller.setCenter(startPoint)
                            val marker = Marker(map)
                            marker.position = startPoint
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            map.overlays.add(marker)
                        }
                    }
                }
            }
        }

        // Set OnClickListener for Save button
        buttonSave.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val note = Note(
                    noteEditTitle.editableText.toString(),
                    noteEditMessage.editableText.toString(),
                    currentPhotoPath.toString()
                )
                if (noteId != -1) {
                    note.id = noteId
                    noteDao.update(note)
                } else {
                    noteDao.insertAll(note)
                }


                runOnUiThread {
                    Toast.makeText(
                        this@NoteEditActivity,
                        "Note saved",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            }
        }

        // Set OnClickListener for Camera button
        buttonCamera.setOnClickListener {
            if (checkAndRequestPermissions()) {
                dispatchTakePictureIntent()
            }
        }

        // Set OnClickListener for Gallery button
        buttonGallery.setOnClickListener {
            if (checkAndRequestPermissions()) {
                val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK)
            }
        }


        // Request location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        } else {
            getLastLocation()
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnCompleteListener(OnCompleteListener<Location> { task ->
                if (task.isSuccessful && task.result != null) {
                    val location: Location? = task.result
                    location?.let {
                        val latitude = it.latitude
                        val longitude = it.longitude
                        latitudeTextView.text = "Latitude: $latitude"
                        longitudeTextView.text = "Longitude: $longitude"
                        saveLocationToDatabase(latitude, longitude)
                        val startPoint = GeoPoint(latitude, longitude)
                        map.controller.setZoom(15.0)
                        map.controller.setCenter(startPoint)
                        val marker = Marker(map)
                        marker.position = startPoint
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        map.overlays.add(marker)
                    }
                } else {
                    Toast.makeText(this, "Failed to get location", Toast.LENGTH_LONG).show()
                }
            })
        }
    }


    private fun saveLocationToDatabase(latitude: Double, longitude: Double) {
        note?.let {
            it.latitude = latitude
            it.longitude = longitude
            CoroutineScope(Dispatchers.IO).launch {
                noteDao.update(it)
            }
        }
    }

    private fun checkAndRequestPermissions(): Boolean {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val listPermissionsNeeded = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        return if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), 0)
            false
        } else {
            true
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                Log.e("NoteEditActivity", "Error occurred while creating the file", ex)
                null
            }
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "com.onenote.android.fileprovider",
                    it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        } else {
            Log.e("NoteEditActivity", "No activity found to handle the intent")
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File = getExternalFilesDir(null)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val file = File(currentPhotoPath)
                    val uri = Uri.fromFile(file)
                    imagePreview.setImageURI(uri)
                }
                REQUEST_IMAGE_PICK -> {
                    val imageUri = data?.data
                    imageUri?.let {
                        currentPhotoPath = getPathFromUri(it)
                        imagePreview.setImageURI(it)
                    }
                }
            }
        }
    }


    private fun getPathFromUri(uri: Uri): String {
        var path = ""
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                path = cursor.getString(columnIndex)
            }
        }
        return path
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        } else if (item.itemId == R.id.delete) {
            showDeleteDialog()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.delete_message))
            .setPositiveButton(R.string.yes) { _, _ ->
                val noteId = intent.getIntExtra("noteId", -1)
                if (noteId != -1) {
                    val note = noteDao.loadAllByIds(noteId).firstOrNull()
                    note?.let {
                        noteDao.delete(it)
                    }
                }
                finish()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }


    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }
}
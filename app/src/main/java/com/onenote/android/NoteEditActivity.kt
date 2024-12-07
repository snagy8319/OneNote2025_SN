package com.onenote.android

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.room.Room
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
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

/**
 * Activity for editing a note.
 * Allows the user to edit the note's title, message, and image, and to save the note.
 */
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

    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initConfiguration()
        setContentView(R.layout.activity_note_edit)
        setupToolbar()
        initializeDatabase()
        initializeViews()
        checkAndLoadNote()
        setupButtonListeners()
        requestLocationPermissions()
    }

    /**
     * Initializes the configuration for the osmdroid library.
     */
    private fun initConfiguration() {
        val configuration = Configuration.getInstance()
        configuration.userAgentValue = BuildConfig.APPLICATION_ID
        configuration.load(this, PreferenceManager.getDefaultSharedPreferences(this))
    }

    /**
     * Sets up the toolbar with a back button.
     */
    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
    }

    /**
     * Initializes the Room database and retrieves the NoteDao.
     */
    private fun initializeDatabase() {
        val db = Room.databaseBuilder(
            applicationContext,
            NotesDatabase::class.java, "notes"
        ).allowMainThreadQueries().build()
        noteDao = db.noteDao()
    }

    /**
     * Finds and initializes the views by their IDs.
     */
    private fun initializeViews() {
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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    /**
     * Checks if a note is being edited and loads the note details if applicable.
     */
    private fun checkAndLoadNote() {
        noteId = intent.getIntExtra("noteId", -1)
        if (noteId != -1) {
            CoroutineScope(Dispatchers.IO).launch {
                note = noteDao.loadAllByIds(noteId).firstOrNull()
                note?.let { runOnUiThread { populateNoteDetails(it) } }
            }
        }
    }

    /**
     * Populates the note details in the UI.
     * @param note The note to populate the details from.
     */
    private fun populateNoteDetails(note: Note) {
        noteEditTitle.setText(note.title)
        noteEditMessage.setText(note.message)
        note.image?.let { path ->
            val file = File(path)
            if (file.exists()) {
                val uri = Uri.fromFile(file)
                imagePreview.setImageURI(uri)
            }
        }
        note.latitude?.let { latitude ->
            note.longitude?.let { longitude ->
                latitudeTextView.text = "Latitude: $latitude"
                longitudeTextView.text = "Longitude: $longitude"
                val startPoint = GeoPoint(latitude, longitude)
                map.controller.setZoom(15.0)
                map.controller.setCenter(startPoint)
                val marker = Marker(map)
                marker.position = startPoint
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                map.overlays.add(marker)
            }
        }
    }

    /**
     * Sets up the click listeners for the save, camera, and gallery buttons.
     */
    private fun setupButtonListeners() {
        buttonSave.setOnClickListener { saveNote() }
        buttonCamera.setOnClickListener { if (checkAndRequestPermissions()) dispatchTakePictureIntent() }
        buttonGallery.setOnClickListener { if (checkAndRequestPermissions()) dispatchPickPictureIntent() }
    }

    /**
     * Saves the note to the database.
     */
    private fun saveNote() {
        CoroutineScope(Dispatchers.IO).launch {
            val note = Note(
                noteEditTitle.editableText.toString(),
                noteEditMessage.editableText.toString(),
                currentPhotoPath.toString()
            ).apply { if (noteId != -1) id = noteId }
            if (noteId != -1) noteDao.update(note) else noteDao.insertAll(note)
            runOnUiThread {
                Toast.makeText(this@NoteEditActivity, "Note saved", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    /**
     * Dispatches an intent to pick an image from the gallery.
     */
    private fun dispatchPickPictureIntent() {
        val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK)
    }

    /**
     * Requests location permissions if not already granted.
     */
    private fun requestLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        } else {
            getLastLocation()
        }
    }

    /**
     * Retrieves the last known location of the device and updates the UI and database.
     */
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

    /**
     * Saves the location to the database.
     * @param latitude The latitude to save.
     * @param longitude The longitude to save.
     */
    private fun saveLocationToDatabase(latitude: Double, longitude: Double) {
        note?.let {
            it.latitude = latitude
            it.longitude = longitude
            CoroutineScope(Dispatchers.IO).launch { noteDao.update(it) }
        }
    }

    /**
     * Checks and requests necessary permissions for camera and storage access.
     * @return True if permissions are already granted, false otherwise.
     */
    private fun checkAndRequestPermissions(): Boolean {
        val permissions = mutableListOf(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        val listPermissionsNeeded = permissions.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        return if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), 0)
            false
        } else {
            true
        }
    }

    /**
     * Dispatches an intent to capture an image using the camera.
     */
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val photoFile: File? = try { createImageFile() } catch (ex: IOException) {
                Log.e("NoteEditActivity", "Error occurred while creating the file", ex)
                null
            }
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(this, "com.onenote.android.fileprovider", it)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        } else {
            Log.e("NoteEditActivity", "No activity found to handle the intent")
        }
    }

    /**
     * Creates an image file to store the captured image.
     * @return The created image file.
     * @throws IOException If an error occurs while creating the file.
     */
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File = getExternalFilesDir(null)!!
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply { currentPhotoPath = absolutePath }
    }

    /**
     * Handles the result of the image capture or image pick activity.
     * @param requestCode The request code.
     * @param resultCode The result code.
     * @param data The intent data.
     */
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

    /**
     * Retrieves the file path from a URI.
     * @param uri The URI to retrieve the path from.
     * @return The file path.
     */
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

    /**
     * Creates the options menu.
     * @param menu The options menu.
     * @return True if the menu is created, false otherwise.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Handles options item selection.
     * @param item The selected menu item.
     * @return True if the item is handled, false otherwise.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.delete -> {
                showDeleteDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Shows a dialog to confirm note deletion.
     */
    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.delete_message))
            .setPositiveButton(R.string.yes) { _, _ ->
                val noteId = intent.getIntExtra("noteId", -1)
                if (noteId != -1) {
                    val note = noteDao.loadAllByIds(noteId).firstOrNull()
                    note?.let { noteDao.delete(it) }
                }
                finish()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    /**
     * Resumes the MapView.
     */
    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    /**
     * Pauses the MapView.
     */
    override fun onPause() {
        super.onPause()
        map.onPause()
    }
}
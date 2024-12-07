package com.onenote.android

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.Settings
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig

/**
 * MainActivity is the entry point of the application.
 * It displays the main screen with a login button and an animated icon.
 */
class MainActivity : AppCompatActivity() {
    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID

      /*  val mediaPlayer = MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI)
        mediaPlayer.start()
*/
        // Find views by IDs
        val buttonLogin = findViewById<Button>(R.id.login)
        val imageViewIcon = findViewById<ImageView>(R.id.icon)

        // Animate Icon
        val animationRotation = AnimationUtils.loadAnimation(this, R.anim.rotate)
        imageViewIcon.startAnimation(animationRotation)

        // Set OnClickListener
        buttonLogin.setOnClickListener{
            // Show Signed-In Toast
            Toast.makeText(this, R.string.signed_in, Toast.LENGTH_LONG).show()

            // Create Intent and open NoteListActivity
            val intent = Intent(this, NoteListActivity::class.java)
            startActivity(intent)

            // Finish MainActivity
            finish()
        }
    }
}
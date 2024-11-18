package com.onenote.android

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonLogin = findViewById<Button>(R.id.login)
        val imageViewIcon = findViewById<ImageView>(R.id.icon)
        val animationRotation = AnimationUtils.loadAnimation(this, R.anim.rotate)

        buttonLogin.setOnClickListener{
            imageViewIcon.startAnimation(animationRotation)
            Toast.makeText(this, R.string.signed_in, Toast.LENGTH_LONG).show()
        }
    }
}
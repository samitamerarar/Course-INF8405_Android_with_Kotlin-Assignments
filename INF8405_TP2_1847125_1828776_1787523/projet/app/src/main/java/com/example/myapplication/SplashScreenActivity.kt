package com.example.myapplication

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

// Splash screen
class SplashScreenActivity : AppCompatActivity() {
    private var introSound : MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        introSound = MediaPlayer.create(this, R.raw.intro)
        supportActionBar!!.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splashscreen)
        introSound!!.start()

        Handler().postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3000
        )
    }
}

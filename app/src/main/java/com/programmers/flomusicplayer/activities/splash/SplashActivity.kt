package com.programmers.flomusicplayer.activities.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.programmers.flomusicplayer.R
import com.programmers.flomusicplayer.activities.song.detail.FloSongDetailActivity

class SplashActivity : AppCompatActivity() {

    private val SPLASH_VISIBLE_TIME = 2000L; // ms

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({ moveToDetail() }, SPLASH_VISIBLE_TIME)
    }

    private fun moveToDetail() {
        val intent = Intent(this, FloSongDetailActivity::class.java)
        startActivity(intent);
        finish()
    }
}
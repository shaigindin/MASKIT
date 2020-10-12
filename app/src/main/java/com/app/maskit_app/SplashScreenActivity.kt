package com.app.maskit_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import utils.BitmapUtils


class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val iv_note: ImageView = findViewById(R.id.iv_note)
        iv_note.setImageBitmap(BitmapUtils.getBitmapFromAssets(this, "ic_launcher.png", 300, 300))
        iv_note.alpha = 0f
        iv_note.animate().setDuration(1500).alpha(1f).withEndAction(){
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }

    }
}
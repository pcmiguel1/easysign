package com.pcmiguel.easysign

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.concurrent.thread

class LoadingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        Handler(Looper.getMainLooper()).postDelayed({
            thread {
                runOnUiThread {
                    startMainActivity()
                }
            }
        }, 3000)

    }

    private fun startMainActivity() {

        runOnUiThread {

            val intent = Intent(applicationContext, MainActivity::class.java)

            if (getIntent() != null && getIntent().extras != null)
                intent.putExtras(getIntent().extras!!)

            startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
            finish()


        }

    }

}
package com.zebra.rfid.demo.sdksample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.zebra.rfid.demo.sdksample.R
import android.content.Intent
import android.view.View
import com.zebra.rfid.demo.sdksample.MainActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        findViewById<View>(R.id.btnGo).setOnClickListener { view: View? ->
            startActivity(
                Intent(
                    this@SplashActivity,
                    MainActivity::class.java
                )
            )
        }
    }
}
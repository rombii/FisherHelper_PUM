package com.example.fisherhelper

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_maps.*


class MainActivity : AppCompatActivity() {
    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        map_button.setOnClickListener {
          val intent = Intent(this,MapsActivity::class.java)
            startActivity(intent)


        }

        scan_button.setOnClickListener {
            val intent = Intent(this,ScannerActivity::class.java)
            startActivity(intent)
        }

        weather_button.setOnClickListener {
            val intent = Intent(this, WeatherActivity::class.java)
            startActivity(intent)
        }



    }
}
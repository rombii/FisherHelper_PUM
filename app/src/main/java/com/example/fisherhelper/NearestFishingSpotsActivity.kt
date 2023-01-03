package com.example.fisherhelper

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.GeoPoint

class NearestFishingSpotsActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearest_fishing_spots)

        val nearestFishingSpots =
            intent.getParcelableArrayListExtra<FishingSpot>("nearestFishingSpots")!!.toList()
        val nearestFishingSpotsListView =
            findViewById<ListView>(R.id.nearest_fishing_spots_list_view)
//        nearestFishingSpotsListView.adapter = ArrayAdapter(
//            this, android.R.layout.simple_list_item_1, nearestFishingSpots!!.map { it.name }
//        )
        nearestFishingSpotsListView.adapter = FishingSpotsAdapter(
            this,
            nearestFishingSpots
        )
    }


}


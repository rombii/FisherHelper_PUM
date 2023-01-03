package com.example.fisherhelper

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class FishingSpotsAdapter(context: Context, private val fishingSpots: List<FishingSpot>) :
    ArrayAdapter<FishingSpot>(context, 0, fishingSpots) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Pobierz element z tablicy nearestFishingSpots
        val fishingSpot = getItem(position)

        // Utwórz lub odśwież widok elementu listy
        val view = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.nearest_fishing_spots_list_item,
            parent,
            false
        )

        // Pobierz widoki tekstowe z widoku elementu listy
        val numberTextView = view.findViewById<TextView>(R.id.number_text_view)
        val nameTextView = view.findViewById<TextView>(R.id.name_text_view)
        val distanceTextView = view.findViewById<TextView>(R.id.distance_text_view)


        // Ustaw tekst w widokach tekstowych
        numberTextView.text = "${position + 1}."
        nameTextView.text = fishingSpot!!.name
        distanceTextView.text = "${String.format("%.2f",fishingSpot.distanceFromCurrentLocation)} km"



        return view
    }
}
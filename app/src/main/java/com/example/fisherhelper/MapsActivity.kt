package com.example.fisherhelper

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.Request

import com.android.volley.toolbox.Volley
import com.example.fisherhelper.databinding.ActivityMapsBinding

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.activity_maps.*
import org.json.JSONException
import org.json.JSONObject


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, OnMapsSdkInitializedCallback {


    private lateinit var mMap: GoogleMap

    private lateinit var binding: ActivityMapsBinding

   public lateinit var currentLocation: Location

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val permissionCode = 101

    private lateinit var temperatureTextView: TextView
    private lateinit var weatherDescriptionTextView: TextView
    companion object {
        // Klucz API dla OpenWeather
        private const val OPEN_WEATHER_MAP_API_URL = "https://api.openweathermap.org/data/2.5/weather?"
        private const val OPEN_WEATHER_MAP_API_KEY = "3b76f82e8a13f592648058112756e765"

        // Klucz API dla map Google
        private const val GOOGLE_MAPS_API_KEY = "AIzaSyDS43-gCdwbbHLm-ndHMM22mObMQgJ3NiE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST, this)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        temperatureTextView = findViewById(R.id.temperature_text_view)
        weatherDescriptionTextView = findViewById(R.id.weather_description_text_view)

        getCurrentLocationUser()

        getMarkersFromFireStore()




    }

    private fun getMarkersFromFireStore() {

        val db = FirebaseFirestore.getInstance()
        val ref = db.collection("coordinates")
        ref.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // document exists
                    val geopoint = document.getGeoPoint("geoPoint")
                    val name = document.get("name")
                    val latLng = LatLng(geopoint!!.latitude, geopoint!!.longitude)
                mMap.addMarker(
                        MarkerOptions().position(latLng).title(name as String?)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.gear))
                    )


                }
            }
    }


    public fun getCurrentLocationUser() {
        if (ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), permissionCode
            )
            return
        }
        val getLocation =
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLocation = location


                    val mapFragment = supportFragmentManager
                        .findFragmentById(R.id.map) as SupportMapFragment
                    mapFragment.getMapAsync(this)

                }
            }


    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
      //  val uiSettings = googleMap.uiSettings

      //  uiSettings.isMyLocationButtonEnabled = true

      //  val latLng = LatLng(currentLocation.latitude ,currentLocation.longitude)
       val latLng = LatLng(50.029488, 22.008091) //example location of rzeszow
        val location = latLng.latitude.toString()
        val location1 = latLng.longitude.toString()
        val markerOptions = MarkerOptions().position(latLng)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            .title("Twoja lokalizacja $location $location1")
        googleMap?.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
        googleMap?.addMarker(markerOptions)
        checkWeather(latLng!!.latitude, latLng!!.longitude)
        val db = FirebaseFirestore.getInstance()
        // pobranie referencji do kolekcji w Firestore
        val collectionReference = db.collection("coordinates")


// utworzenie obiektu GoogleMap za pomocą MapView


        // wyświetlenie markera na mapie
        val marker = googleMap.addMarker(
            MarkerOptions().position(LatLng(0.0, 0.0)).title("Czy tutaj chcesz dodać łowisko?")
        )

        // nasłuchiwanie kliknięć na mapie
        mMap.setOnMapClickListener { latLng ->
            // ustawienie pozycji markera na klikniętym miejscu na mapie
            marker!!.position = latLng
        }



        add_marker_button.setOnClickListener {
            fun onClick() {
                if (marker!!.position.latitude == 0.0 && marker.position.longitude == 0.0) {
                    Toast.makeText(this, "Nie ustawiłeś markera, kliknij na dany punkt mapy", Toast.LENGTH_SHORT)
                        .show()
                    return
                }
                if (marker_name_input.text.isEmpty()) {
                    Toast.makeText(this, "Nie wpisano nazwy łowiska", Toast.LENGTH_SHORT).show()
                    return
                }

                // pobranie koordynatów markera
                val latLng = marker!!.position

                // pobranie nazwy markera z pola tekstowego
                val markerName = marker_name_input.text.toString()

                // utworzenie obiektu GeoPoint z koordynatami markera
                val markerGeoPoint = GeoPoint(latLng.latitude, latLng.longitude)
                collectionReference.add(
                    mapOf(
                        "name" to markerName,
                        "geoPoint" to markerGeoPoint
                    )
                )
                    .addOnSuccessListener {
                        // wyświetlenie komunikatu o powodzeniu dodawania markera
                        getMarkersFromFireStore()
                        val toast =
                            Toast.makeText(this, "Łowisko dodane pomyślnie", Toast.LENGTH_SHORT)
                                .show()
//                    val view = toast.view
//                    view!!.setBackgroundColor(Color.BLUE)
//                    toast.show()
                        marker.position = LatLng(0.0, 0.0)
                        // wyczyszczenie pola tekstowego z nazwą markera
                        marker_name_input.setText("")

                    }

                    .addOnFailureListener { exception ->
                        // wyświetlenie komunikatu o błędzie podczas dodawania markera
                        Toast.makeText(
                            this,
                            "Błąd podczas dodawania markera: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }


            }
            onClick()
        }
        val showNearestFishingSpotsButton = findViewById<Button>(R.id.show_nearest_fishing_spots_button)
        showNearestFishingSpotsButton.setOnClickListener {
            findNearestFishingSpots(currentLocation)
        }

    }

    public fun checkWeather(lat: Double, lon: Double) {
        val url = "$OPEN_WEATHER_MAP_API_URL&lat=$lat&lon=$lon&appid=$OPEN_WEATHER_MAP_API_KEY"

        val request = StringRequest(
            Request.Method.GET, url,
            Response.Listener { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    val main = jsonResponse.getJSONObject("main")
                    val temperature = main.getDouble("temp")
                    val celsjus =temperature - 273.15
                    val windSpeed = jsonResponse.getJSONObject("wind").getDouble("speed")
                    val weather = jsonResponse.getJSONArray("weather").getJSONObject(0).getString("main")
                    val isGoodWeather = isGoodWeather(temperature, windSpeed, weather)
                    temperatureTextView.text = "Temperatura: "+String.format("%.0f",celsjus)+"°C"
                 //   weatherDescriptionTextView.text = "Opis pogody: $weather"
                    if (isGoodWeather) {
                        weatherDescriptionTextView.text = "Pogoda na łowienie: doskonała"
                    } else {
                        weatherDescriptionTextView.text = "Pogoda na łowienie: zła"
                    }
                } catch (e: JSONException) {
// Tutaj możesz obsłużyć wyjątek, jeśli odpowiedź API jest niepoprawna lub nie można jej przetworzyć.
                }
            },
            Response.ErrorListener { error ->
// Tutaj możesz obsłużyć błąd, jeśli żądanie do API zostało odrzucone lub nie udało się je wysłać.
            }
        )
        val queue = Volley.newRequestQueue(this)
        queue.add(request)
    }

    private fun isGoodWeather(temperature: Double, windSpeed: Double, weather: String): Boolean {

        return temperature > 0 && windSpeed < 20 && weather != "Rain"
    }

    private fun findNearestFishingSpots(currentLocation: Location) {
        val db = FirebaseFirestore.getInstance()
        val ref = db.collection("coordinates")
        ref.get()
            .addOnSuccessListener { documents ->
                // Pobierz listę łowisk
                val fishingSpots = mutableListOf<FishingSpot>()
                for (document in documents) {
                    val geopoint = document.getGeoPoint("geoPoint")
                    val latLng = LatLng(geopoint!!.latitude, geopoint!!.longitude)
                    val latlng2 = LatLng(currentLocation.latitude, currentLocation.longitude)
                    val name = document.get("name")

                    val fishingSpot = FishingSpot(name as String, latLng,latlng2)
                    fishingSpots.add(fishingSpot)
                }

                // Posortuj łowiska według odległości od obecnej lokalizacji użytkownika
                fishingSpots.sortBy { fishingSpot ->
                    val distance = FloatArray(1)
                    val latLng2 = LatLng(50.029488, 22.008091)
                    Location.distanceBetween(
                   //    currentLocation.latitude, currentLocation.longitude,
                        latLng2.latitude,latLng2.longitude,
                        fishingSpot.latLng.latitude, fishingSpot.latLng.longitude, distance
                    )
                    distance[0]
                }
                // Wyświetl pięć najbliższych łowisk
                val nearestFishingSpots = fishingSpots.take(5)
                val intent = Intent(this, NearestFishingSpotsActivity::class.java)
                intent.putParcelableArrayListExtra("nearestFishingSpots", ArrayList(nearestFishingSpots))
                startActivity(intent)
            }
    }

    override fun onMapsSdkInitialized(renderer: MapsInitializer.Renderer) {
        when (renderer) {
            MapsInitializer.Renderer.LATEST -> Log.d("MapsDemo", "The latest version of the renderer is used.")
            MapsInitializer.Renderer.LEGACY -> Log.d("MapsDemo", "The legacy version of the renderer is used.")
        }
    }



}
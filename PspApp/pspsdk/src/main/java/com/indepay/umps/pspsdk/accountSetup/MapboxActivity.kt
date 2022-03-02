package com.indepay.umps.pspsdk.accountSetup

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.baseActivity.SdkBaseActivity
import com.indepay.umps.pspsdk.models.GoogleMapsDTO
import com.utsman.samplegooglemapsdirection.kotlin.model.DirectionResponses
import kotlinx.android.synthetic.main.activity_mapbox.*
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class MapboxActivity: SdkBaseActivity(), OnMapReadyCallback,GoogleMap.OnMarkerClickListener {


    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false


    private lateinit var location1: LatLng
    private lateinit var location2: LatLng



//     location1 = LatLng(13.0356745, 77.5881522)
//     TamWorth = LatLng(9.89, 78.11)
//     location2 = LatLng(13.029727, 77.5933021)
//    var Brisbane = LatLng(-27.470125, 153.021072)
    var distance: Double? = null


    private var locationArrayList: ArrayList<LatLng>? = null

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
        private const val PLACE_PICKER_REQUEST = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapbox)

        location1 = LatLng(13.0356745, 77.5881522)
        location2 = LatLng(13.029727, 77.5933021)

        locationArrayList = ArrayList()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment

        val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager
        search_view.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        search_view.setOnQueryTextListener(object : android.support.v7.widget.SearchView.OnQueryTextListener, android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                // location name from search view.

                // location name from search view.
                val location: String = search_view.getQuery().toString()

                // below line is to create a list of address
                // where we will store the list of all address.

                // below line is to create a list of address
                // where we will store the list of all address.
                var addressList: List<Address>? = null

                // checking if the entered location is null or not.
                  map.clear()
                // checking if the entered location is null or not.
                if (location != null || location == "") {
                    // on below line we are creating and initializing a geo coder.
                    val geocoder = Geocoder(this@MapboxActivity)
                    try {
                        // on below line we are getting location from the
                        // location name and adding that location to address list.
                        addressList = geocoder.getFromLocationName(location, 1)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    // on below line we are getting the location
                    // from our list a first position.
                    for(i in 0..addressList!!.size-1) {
                        val address = addressList!![i]


                        // on below line we are creating a variable for our location
                        // where we will add our locations latitude and longitude.
                        val latLng = LatLng(address.latitude, address.longitude)

                        // on below line we are adding marker to that position.
                        map.addMarker(MarkerOptions().position(latLng).title(location))

                        // below line is to animate camera to that position.
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
                    }
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                return false
            }

        })
        mapFragment.getMapAsync(this)


        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                lastLocation = p0.lastLocation
                placeMarkerOnMap(LatLng(lastLocation.latitude, lastLocation.longitude))
            }

        }
        createLocationRequest()

        locationArrayList!!.add(location1)
        locationArrayList!!.add(location2)
//        locationArrayList!!.add(NewCastle)
//        locationArrayList!!.add(Brisbane)

        back_arrowimage.setOnClickListener{

            onBackPressed()
        }

        get_direction.setOnClickListener {
            code_layout.visibility = View.GONE


            val locationfirst = location1.latitude.toString() + "," + location1.longitude.toString()
            val locationaecond = location2.latitude.toString() + "," + location2.longitude.toString()


            val apiServices = RetrofitClient.apiServices(this)
            apiServices.getDirection(locationfirst, locationaecond, "AIzaSyBsE-TUEpWts6Wvmcwx8ZxD023fhJJF5QY")
                    .enqueue(object : Callback<DirectionResponses> {
                        override fun onResponse(call: Call<DirectionResponses>, response: Response<DirectionResponses>) {
                            drawPolyline(response)
                            Log.d("Sudhir Response::", response.message())
                        }

                        override fun onFailure(call: Call<DirectionResponses>, t: Throwable) {
                            Log.e("error", t.localizedMessage)
                        }
                    })



        }

    }
        override fun onMapReady(googleMap: GoogleMap) {

                map = googleMap

            map.uiSettings.isZoomControlsEnabled = true
            map.setOnMarkerClickListener(this)



            setUpMap()

//            distance = SphericalUtil.computeDistanceBetween(sydney, NewCastle);
//            Toast.makeText(this, "Distance between Sydney and Brisbane is \n " + String.format("%.2f", distance!! / 1000) + "km", Toast.LENGTH_SHORT).show();

//            val URL = getDirectionURL(sydney, NewCastle)
//            Log.d("Sudhir","Map URL::"+URL)
//            GetDirection(URL).execute()


        }
        override fun onMarkerClick(marker: Marker?): Boolean {
       var markerName:String = marker!!.getTitle();
            Toast.makeText(this, "Clicked location is " + markerName, Toast.LENGTH_SHORT).show();
            convertAddress(markerName)
            return false
        }


    fun convertAddress(address: String) {
        val geocoder: Geocoder = Geocoder(this)
        if (address != null && !address.isEmpty()) {
            try {
                val addressList: List<Address> = geocoder.getFromLocationName(address, 1)
                if (addressList != null && addressList.size > 0) {
                    val lat = addressList[0].latitude
                    val lng = addressList[0].longitude
                    Log.d("Sudhir","Selected Lat::"+lat)
                    Log.d("Sudhir","Selected Lng::"+lng)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } // end catch
        } // end if
    } // end convertAddress

    private fun setUpMap() {
            if (ActivityCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
                return
            }

//            map.isMyLocationEnabled = true
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
                // Got last known location. In some rare situations this can be null.
//                if (location != null) {
//                    lastLocation = location
//                    val currentLatLng = LatLng(location.latitude, location.longitude)
//                    placeMarkerOnMap(currentLatLng)
//                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))

                    for (i in 0..locationArrayList!!.size-1) {

                        val currentLatLng = LatLng(locationArrayList!!.get(i).latitude, locationArrayList!!.get(i).longitude)
                    placeMarkerOnMap(currentLatLng)

                        // below line is use to add marker to each location of our array list.
//                        map.addMarker(MarkerOptions().position(locationArrayList!![i]))
//              // below lin is use to zoom our camera on map.
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))

                        // below line is use to move our camera to the specific location.
//                        map.moveCamera(CameraUpdateFactory.newLatLng(locationArrayList!![i]))
                    }
//                }
            }
        }

        private fun placeMarkerOnMap(location: LatLng) {
            val markerOptions = MarkerOptions().position(location)
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.icon_location_idle)))


            val titleStr = getAddress(location)
            markerOptions.title(titleStr)
            Log.d("Sudhir","GetAddress title::"+titleStr)

            map.addMarker(markerOptions)
        }

        private fun getAddress(latLng: LatLng): String {

            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses: List<Address>?
            val address: Address?
            var addressText = ""

            try {

                addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (null != addresses && !addresses.isEmpty()) {
                    address = addresses[0]
                    Log.d("Sudhir","address::"+address)
                    for (i in 0..address.maxAddressLineIndex) {
                        addressText += if (i == 0) address.getAddressLine(i) else "\n" + address.getAddressLine(i)
                        Log.d("Sudhir","addresstext::"+addressText)
                    }
                }
            } catch (e: IOException) {
                Log.e("MapsActivity", e.localizedMessage)
            }

            return addressText
        }

        private fun startLocationUpdates() {
            if (ActivityCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        LOCATION_PERMISSION_REQUEST_CODE)
                return
            }
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
        }

        private fun createLocationRequest() {

            locationRequest = LocationRequest()

            locationRequest.interval = 10000

            locationRequest.fastestInterval = 5000
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

            val builder = LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest)


            val client = LocationServices.getSettingsClient(this)
            val task = client.checkLocationSettings(builder.build())


            task.addOnSuccessListener {
                locationUpdateState = true
                startLocationUpdates()
            }
            task.addOnFailureListener { e ->

                if (e is ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        e.startResolutionForResult(this,
                                REQUEST_CHECK_SETTINGS)
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignore the error.
                    }
                }
            }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == REQUEST_CHECK_SETTINGS) {
                if (resultCode == Activity.RESULT_OK) {
                    locationUpdateState = true
                    startLocationUpdates()
                }
            }
            if (requestCode == PLACE_PICKER_REQUEST) {
                if (resultCode == RESULT_OK) {
                    val place = PlacePicker.getPlace(this, data)
                    var addressText = place.name.toString()
                    addressText += "\n" + place.address.toString()

                    placeMarkerOnMap(place.latLng)
                }
            }

        }


        override fun onPause() {
            super.onPause()
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }


        public override fun onResume() {
            super.onResume()
            if (!locationUpdateState) {
                startLocationUpdates()
            }
        }

        private fun loadPlacePicker() {
            val builder = PlacePicker.IntentBuilder()

            try {
                startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST)
            } catch (e: GooglePlayServicesRepairableException) {
                e.printStackTrace()
            } catch (e: GooglePlayServicesNotAvailableException) {
                e.printStackTrace()
            }
        }

    fun getDirectionURL(origin: LatLng, dest:LatLng):String{

        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&sensor=false&mode=driving&key=AIzaSyBsE-TUEpWts6Wvmcwx8ZxD023fhJJF5QY"
    }

    inner class GetDirection(val url : String): AsyncTask<Void,Void,List<List<LatLng>>>(){
        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body.toString()
            val result = ArrayList<List<LatLng>>()
            try {
                val respObj = Gson().fromJson(data, GoogleMapsDTO::class.java)
                val path = ArrayList<LatLng>()

                for (i in 0..(respObj.routes[0].legs[0].steps.size - 1)) {
//                    val startLatLng = LatLng(respObj.routes[0].legs[0].steps[i].start_location.lat.toDouble(),
//                            respObj.routes[0].legs[0].steps[i].start_location.lng.toDouble())
//
//                    path.add(startLatLng)
//
//                    val endLatLng = LatLng(respObj.routes[0].legs[0].steps[i].end_location.lat.toDouble(),
//                            respObj.routes[0].legs[0].steps[i].end_location.lng.toDouble())
//                    path.add(endLatLng)
                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))

                }
                Log.d("Sudhir", "Path")
                result.add(path)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: List<List<LatLng>>) {
            val lineoptions = PolylineOptions()
            for (i in result.indices){
               lineoptions.addAll(result[i])
               lineoptions.width(10f)
               lineoptions.color(Color.BLUE)
               lineoptions.geodesic(true)

           }
            map.addPolyline(lineoptions)
        }

        fun decodePolyline(encoded: String):List<LatLng>{
            val poly = ArrayList<LatLng>()
            var index =0
            val len = encoded.length
            var lat =0
            var lng =0

            while (index<len){
                var b:Int
                var shift =0
                var result =0
                do {
                    b = encoded[index++].toInt() - 63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                }
                    while (b>= 0x20)
                val dlat = if(result and 1 !=0) (result shr 1).inv() else result shr 1
                lat +=dlat

                shift =0
                result =0
                do{
                    b= encoded[index++].toInt()-63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                }
                while (b>= 0x20)
                val dlng = if(result and 1 !=0) (result shr 1).inv() else result shr 1
                lng +=dlng

                val latLng = LatLng((lat.toDouble() / 1E5) , (lng.toDouble() / 1E5))
//                val hm = java.util.HashMap<String,String>()
//                hm["lat"] = (lat.toDouble() / 1E5).toString()
//                hm["lng"] = (lng.toDouble() / 1E5).toString()
                poly.add(latLng)
            }
            return poly
        }
    }


    private fun drawPolyline(response: Response<DirectionResponses>) {
        val shape = response.body()?.routes?.get(0)?.overviewPolyline?.points
        val polyline = PolylineOptions()
                .addAll(PolyUtil.decode(shape))
                .width(8f)
                .color(Color.RED)
        map.addPolyline(polyline)
    }

    private interface ApiServices {
        @GET("maps/api/directions/json")
        fun getDirection(@Query("origin") origin: String,
                         @Query("destination") destination: String,
                         @Query("key") apiKey: String): Call<DirectionResponses>
    }

    private object RetrofitClient {
        fun apiServices(context: Context): ApiServices {
            val retrofit = Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(context.resources.getString(R.string.base_url))
                    .build()

            return retrofit.create<ApiServices>(ApiServices::class.java)
        }
    }
}
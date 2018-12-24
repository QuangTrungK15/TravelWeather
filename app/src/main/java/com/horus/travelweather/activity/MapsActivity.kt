package com.horus.travelweather.activity

import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.util.Log
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.horus.travelweather.R
import com.horus.travelweather.model.PlaceDbO
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class MapsActivity : FragmentActivity(), OnMapReadyCallback {
        /*GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {*/

    private lateinit var mMap: GoogleMap

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    //Getting current location
    private lateinit var lastLocation: Location


    private lateinit var markerPoints:ArrayList<LatLng>
    private lateinit var mGoogleApiClient:GoogleApiClient
    private lateinit var mLastLocation:Location
    private var mCurrLocationMarker: Marker? = null
    private lateinit var mLocationRequest:LocationRequest

    //add a companion object with the code to request location permission
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        //val MY_PERMISSIONS_REQUEST_LOCATION = 99
        //private const val REQUEST_CHECK_SETTINGS = 2 // (RLU) is used as the request code passed to onActivityResult
    }
    /*private fun setUpMap() {
        //The code above checks if the app has been granted the ACCESS_FINE_LOCATION permission. If it hasn’t, then request it from the user.
        //Add a call to setUpMap() at the end of onMapReady().
        //Build and run; click “Allow” to grant permission
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        //Get your current location
        // isMyLocationEnabled = true enables the my-location layer which draws a light blue dot on the user’s location.
        // It also adds a button to the map that, when tapped, centers the map on the user’s location.
        mMap.isMyLocationEnabled = true

        // fusedLocationClient.getLastLocation() gives you the most recent location currently available.
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            // 3
            if (location != null) {
                lastLocation = location

                val geocoder = Geocoder(this, Locale.getDefault())
                try
                {
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                    if (addresses != null)
                    {
                        val returnedAddress = addresses.get(0)
                        val strReturnedAddress = StringBuilder("Address:\n")
                        for (i in 0 until returnedAddress.getMaxAddressLineIndex())
                        {
                            strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
                        }
                        start_location=addresses.get(0).getAddressLine(0)
                        Log.e("start location: ",addresses.get(0).getAddressLine(0))
                    }
                    else
                    {
                        Log.d("a","No Address returned! : ")

                    }
                }
                catch (e:IOException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                    Log.d("a","Canont get Address!")
                }
            }
        }

    }*/


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

/*
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkLocationPermission()
        }

*/
        // Initializing
        markerPoints = ArrayList<LatLng>()
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        setupGoogleMapScreenSettings(googleMap)

        //setUpMap()
        //Get current location
        //The code above checks if the app has been granted the ACCESS_FINE_LOCATION permission. If it hasn’t, then request it from the user.
        //Add a call to setUpMap() at the end of onMapReady().
        //Build and run; click “Allow” to grant permission
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        //Initialize Google Play Services
       /* if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if ((ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED))
            {
                buildGoogleApiClient()
                mMap.isMyLocationEnabled = true
            }
        }
        else
        {
            buildGoogleApiClient()
            mMap.isMyLocationEnabled = true
        }
*/
        //Get your current location
        // isMyLocationEnabled = true enables the my-location layer which draws a light blue dot on the user’s location.
        // It also adds a button to the map that, when tapped, centers the map on the user’s location.
        mMap.isMyLocationEnabled = true


        var currentlocation = LatLng(10.762622, 106.660172)
        var destlocation = LatLng(10.762622, 106.660172)

        // fusedLocationClient.getLastLocation() gives you the most recent location currently available.
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->

            // Got last known location. In some rare situations this can be null.
            // 3
            if (location != null) {
                lastLocation = location

                val geocoder = Geocoder(this, Locale.getDefault())
                try {
                    val addresses = geocoder.getFromLocation(lastLocation.latitude, lastLocation.longitude, 1)

                    if (addresses != null) {
                        val returnedAddress = addresses[0]
                        val strReturnedAddress = StringBuilder("Address:\n")
                        for (i in 0 until returnedAddress.maxAddressLineIndex) {
                            strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
                        }

                        currentlocation = LatLng(addresses[0].latitude, addresses[0].longitude)

                        Log.e("start location: ", currentlocation.toString())

                        //Get Latlng of destinational location (MyPlace from favoritePlaceAvtivity)
                        val place = intent.getSerializableExtra("MyPlace") as PlaceDbO
                        val mGeoDataClient = Places.getGeoDataClient(this)
                        mGeoDataClient.getPlaceById(place.placeId).addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                                val places = task.result
                                val myPlace = places.get(0)
                                destlocation = myPlace.latLng
                                Log.e("end location: ", destlocation.toString())

                                //Add marker -> location
                                markerPoints.add(currentlocation)
                                markerPoints.add(destlocation)

                                // Creating MarkerOptions
                                val options = MarkerOptions()
                                // Setting the position of the marker
                                options.position(currentlocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                mMap.addMarker(options).title = (addresses[0].getAddressLine(0))
                                options.position(destlocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                mMap.addMarker(options).title = (myPlace.address.toString())

                                /**
                                 * For the start location, the color of marker is GREEN and
                                 * for the end location, the color of marker is RED.
                                 */
                                /**
                                 * For the start location, the color of marker is GREEN and
                                 * for the end location, the color of marker is RED.
                                 */
                                // Add new marker to the Google Map Android API V2


                                // Getting URL to the Google Directions API
                                val url = getUrl(currentlocation, destlocation)
                                //val url = getUrl(LatLng(addresses[0].longitude, addresses[0].latitude), myPlace.latLng)
                                Log.d("onMapClick: ", url)
                                val fetchUrl = FetchUrl()
                                Log.d("fetchUrl: ", fetchUrl.toString())
                                // Start downloading json data from Google Directions API
                                fetchUrl.execute(url)
                                //move map camera
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentlocation))
                                mMap.animateCamera(CameraUpdateFactory.zoomTo(12F))

                                places.release()
                                return@addOnCompleteListener
                            } else {
                                Log.e("Notice: ", "Place not found.")
                            }
                        }

                    } else {
                        Log.d("a", "No Address returned! : ")
                    }
                } catch (e: IOException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                    Log.d("a", "Canont get Address!")
                }
            }
        }


    }

    private fun getUrl(origin:LatLng, dest:LatLng):String {
        // Origin of route
        val strOrigin = "origin=" + origin.latitude + "," + origin.longitude
        // Destination of route
        val strDest = "destination=" + dest.latitude + "," + dest.longitude
        // Sensor enabled
        val sensor = "sensor=false"
        // Building the parameters to the web service
        val parameters = "${strOrigin.trim()}&${strDest.trim()}&$sensor"
        // Output format
        val output = "json"
        val apikey="AIzaSyDc6fdew54ONuhKNVCCV6urWWL-1WWMmBI"
        // Building the url to the web service
        val url = "https://maps.googleapis.com/maps/api/directions/$output?$parameters&mode=DRIVING&key=$apikey"
        return url
    }

    // Fetches data from url passed
    private inner class FetchUrl:AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg url:String):String {
            // For storing data from web service
            var data = ""
            try
            {
                // Fetching the data from web service
                data = downloadUrl(url[0])
                Log.d("Background Task data", data.toString())
            }
            catch (e:Exception) {
                Log.d("Background Task", e.toString())
            }
            return data
        }
        override fun onPostExecute(result:String) {
            Log.d("onPostExecue resute", result.toString())
            super.onPostExecute(result)
            var parserTask = ParserTask()
            // Invokes the thread for parsing the JSON data
            parserTask.execute(result)
        }
    }

    /**
     * A method to download json data from url
     */
    @Throws(IOException::class)
    private fun downloadUrl(strUrl: String): String {
        var data = ""
        var iStream: InputStream? = null
        var urlConnection: HttpURLConnection? = null
        try {
            val url = URL(strUrl)

            // Creating an http connection to communicate with url
            urlConnection = url.openConnection() as HttpURLConnection
            Log.d("url connection: ", urlConnection.toString())

            // Connecting to url
            urlConnection.connect()

            // Reading data from url
            iStream = urlConnection.inputStream
            Log.d("iStream: ", iStream.toString())

            data = iStream.bufferedReader().use(BufferedReader::readText)

            iStream.bufferedReader().close()

        } catch (e: Exception) {
            Log.d("Exception downloadUrl", e.toString())
        } finally {
            iStream!!.close()
            urlConnection!!.disconnect()
        }
        return data
    }
    /**
     * A class to parse the Google Places in JSON format
     */
    private inner class ParserTask : AsyncTask<String, Int, List<List<HashMap<String, String>>>>() {

        // Parsing the data in non-ui thread
        override fun doInBackground(vararg jsonData: String): List<List<HashMap<String, String>>>  {

            val jObject: JSONObject?

            try {
                jObject = JSONObject(jsonData[0])

                Log.d("ParserTask", jsonData[0])
                val parser = DataParser()
                Log.d("ParserTask", parser.toString())

                // Starts parsing data
                var routes: List<List<HashMap<String, String>>>  = parser.parse(jObject)
                Log.d("ParserTask", "Executing routes")
                Log.d("ParserTask", routes.toString())
                return routes

            } catch (e: Exception) {
                Log.d("ParserTask", e.toString())
                e.printStackTrace()
            }

            val r:List<List<HashMap<String, String>>> = ArrayList<ArrayList<HashMap<String, String>>>()
            return r
        }

        // Executes in UI thread, after the parsing process
        override fun onPostExecute(result: List<List<HashMap<String, String>>>) {
            var points: ArrayList<LatLng>
            var lineOptions: PolylineOptions? = null

            // Traversing through all the routes
            for (i in result.indices) {
                points = ArrayList<LatLng>()
                lineOptions = PolylineOptions()

                // Fetching i-th route
                val path = result[i]

                // Fetching all the points in i-th route
                for (j in path.indices) {
                    val point = path[j]

                    val lat = java.lang.Double.parseDouble(point["lat"])
                    val lng = java.lang.Double.parseDouble(point["lng"])
                    val position = LatLng(lat, lng)

                    points.add(position)
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points)
                lineOptions.width(12f)
                lineOptions.color(Color.rgb(70, 155, 253))

                Log.d("onPostExecute", "onPostExecute lineoptions decoded")

            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                mMap.addPolyline(lineOptions)
            } else {
                Log.d("onPostExecute", "without Polylines drawn")
            }
        }
    }

    private fun setupGoogleMapScreenSettings(mMap:GoogleMap) {
        mMap.isBuildingsEnabled = true               //Turns the 3D buildings layer on
        mMap.isIndoorEnabled = true                  //Sets whether indoor maps should be enabled.
        //mMap.isTrafficEnabled = true                 //Turns the traffic layer on or off.
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        val mUiSettings = mMap.uiSettings
        mUiSettings.isZoomControlsEnabled = true     //it can be zoom control
        mUiSettings.isCompassEnabled = true          //....compass (la ban)
        mUiSettings.isMyLocationButtonEnabled = true //Enables or disables the my-location layer.
        mUiSettings.isScrollGesturesEnabled = true   //....cử chỉ scroll
        mUiSettings.isZoomGesturesEnabled = true     //...zoom
        mUiSettings.isTiltGesturesEnabled = true     //...Tilt (nghiêng)
        mUiSettings.isRotateGesturesEnabled = true   //...Rotate
        mUiSettings.isMapToolbarEnabled = true       // It ain't working, CHECKKKKKK
    }

   /* @Synchronized private fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
        mGoogleApiClient.connect()
    }
    override fun onConnected(bundle:Bundle?) {
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = 1000
        mLocationRequest.fastestInterval = 1000
        mLocationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        if ((ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED))
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
        }
    }
    override fun onConnectionSuspended(i:Int) {
    }
    override fun onLocationChanged(location:Location) {
        mLastLocation = location
        lastLocation = location
        if (mCurrLocationMarker != null)
        {
            mCurrLocationMarker!!.remove()
        }
        //Place current location marker
        val latLng = LatLng(location.latitude, location.longitude)
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title("Current Position")
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
        mCurrLocationMarker = mMap.addMarker(markerOptions)
        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11F))
        //stop location updates
        if (mGoogleApiClient != null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)
        }
    }
    override fun onConnectionFailed(connectionResult:ConnectionResult) {
    }
    private fun checkLocationPermission():Boolean {
        if ((ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !== PackageManager.PERMISSION_GRANTED))
        {
            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION))
            {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        DirectionsFragment.MY_PERMISSIONS_REQUEST_LOCATION)
            }
            else
            {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        DirectionsFragment.MY_PERMISSIONS_REQUEST_LOCATION)
            }
            return false
        }
        else
        {
            return true
        }
    }
    override fun onRequestPermissionsResult(requestCode:Int,
                                            permissions:Array<String>, grantResults:IntArray) {
        when (requestCode) {
            DirectionsFragment.MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                {
                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if ((ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED))
                    {
                        if (mGoogleApiClient == null)
                        {
                            buildGoogleApiClient()
                        }
                        mMap.isMyLocationEnabled = true
                    }
                }
                else
                {
                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
                }
                return
            }
        }// other 'case' lines to check for other permissions this app might request.
        // You can add here other case statements according to your requirement.
    }*/
}

package com.horus.travelweather.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.horus.travelweather.R
import com.horus.travelweather.adapter.TransportationAdapter
import com.horus.travelweather.database.PlaceData
import com.horus.travelweather.model.TransportationDbO
import kotlinx.android.synthetic.main.activity_directions.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class DirectionsActivity : FragmentActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private val TAG = DirectionsActivity::class.java.simpleName
    private var arrayTransportation : ArrayList<HashMap<String,String>> = arrayListOf()
    private lateinit var adapterTransportation : TransportationAdapter
    var PLACE_AUTOCOMPLETE_REQUEST_CODE = 1
    var PLACE_AUTOCOMPLETE_REQUEST_CODE2 = 2

    private lateinit var mMap:GoogleMap
    private lateinit var markerPoints:ArrayList<LatLng>
    private lateinit var mGoogleApiClient:GoogleApiClient
    private lateinit var mLastLocation:Location
    private var mCurrLocationMarker: Marker? = null
    private lateinit var mLocationRequest:LocationRequest

    var transList = ArrayList<TransportationDbO>()


    var currentlocation = LatLng(10.762622, 106.660172)
    var destlocation = LatLng(10.762622, 106.660172)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_directions)
        //val actionBar = actionBar
        //actionBar.hide()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkLocationPermission()
        }
        // Initializing
        markerPoints = ArrayList<LatLng>()
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val edt_orgin = this.findViewById<View>(R.id.edt_orgin) as TextView
        val edt_destination = this.findViewById<View>(R.id.edt_destination) as TextView

        //val transList = ArrayList<TransportationDbO>()
        transList.add(TransportationDbO("driving",""))
        transList.add(TransportationDbO("walking",""))
        transList.add(TransportationDbO("transit",""))
        implementLoad(transList)

        edt_orgin.setOnClickListener {
            //Filter results by place type (by address: get full address, by establisment: get business address)
            val typeFilter = AutocompleteFilter.Builder()
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT)
                    .build()
            //Use an intent to launch the autocomplete activity (fullscreen mode)
            //https://developers.google.com/places/android-sdk/autocomplete
            val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                    .setFilter(typeFilter)
                    .build(this)
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
        }
        edt_destination.setOnClickListener {
            //Filter results by place type (by address: get full address, by establisment: get business address)
            val typeFilter = AutocompleteFilter.Builder()
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT)
                    .build()
            //Use an intent to launch the autocomplete activity (fullscreen mode)
            //https://developers.google.com/places/android-sdk/autocomplete
            val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                    .setFilter(typeFilter)
                    .build(this)
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE2)
        }
    }

    override fun onMapReady(googleMap:GoogleMap) {
        mMap = googleMap

        setupGoogleMapScreenSettings(googleMap)

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
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
        // Setting onclick event listener for the map
        val fab_directions = this.findViewById<View>(R.id.fab_directions) as FloatingActionButton
        fab_directions.setOnClickListener {

            AddMarker(currentlocation,destlocation)

            // Getting URL to the Google Directions API
            val url = getUrl(currentlocation, destlocation)
            //val url = getUrl(LatLng(addresses[0].longitude, addresses[0].latitude), myPlace.latLng)
            Log.d("onMapClick: ", url)
            val fetchUrl = FetchUrl("driving")
            Log.d("fetchUrl: ", fetchUrl.toString())
            // Start downloading json data from Google Directions API
            fetchUrl.execute(url)

            val url2 = getUrl_Walking(currentlocation, destlocation)
            //val url = getUrl(LatLng(addresses[0].longitude, addresses[0].latitude), myPlace.latLng)
            Log.d("onMapClick2: ", url2)
            val fetchUrl2 = FetchUrl_NotPolyline("walking")
            Log.d("fetchUrl2: ", fetchUrl2.toString())
            // Start downloading json data from Google Directions API
            fetchUrl2.execute(url2)

            val url3 = getUrl_Transit(currentlocation, destlocation)
            //val url = getUrl(LatLng(addresses[0].longitude, addresses[0].latitude), myPlace.latLng)
            Log.d("onMapClick3: ", url3)
            val fetchUrl3 = FetchUrl_NotPolyline("transit")
            Log.d("fetchUrl3: ", fetchUrl3.toString())
            // Start downloading json data from Google Directions API
            fetchUrl3.execute(url3)

            //move map camera
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentlocation))
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12F))
        }

        //When u click anywhere on maps
        mMap.setOnMapClickListener { point ->
            // Already two locations
            if (markerPoints.size > 1) {
                markerPoints.clear()
                mMap.clear()
            }
            // Adding new item to the ArrayList
            markerPoints.add(point)
            // Creating MarkerOptions
            val options = MarkerOptions()
            // Setting the position of the marker
            options.position(point)
            /**
             * For the start location, the color of marker is GREEN and
             * for the end location, the color of marker is RED.
             */
            /**
             * For the start location, the color of marker is GREEN and
             * for the end location, the color of marker is RED.
             */
            if (markerPoints.size == 1) {
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            } else if (markerPoints.size == 2) {
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            }
            // Add new marker to the Google Map Android API V2
            mMap.addMarker(options)

            // Checks, whether start and end locations are captured
            if (markerPoints.size >= 2) {
                val origin = markerPoints[0]
                val dest = markerPoints[1]
                Log.d("origin: ", markerPoints[0].toString())
                Log.d("dest: ", markerPoints[1].toString())



                // Getting URL to the Google Directions API
                val url = getUrl(origin, dest)
                Log.d("onMapClick: ", url)
                val fetchUrl = FetchUrl("1")
                Log.d("fetchUrl: ", fetchUrl.toString())
                // Start downloading json data from Google Directions API
                fetchUrl.execute(url)

                /*val url2 = getUrl_Walking(origin,dest)
                val fetchUrl2 = FetchUrl2("2")
                fetchUrl2.execute(url2)

                val url3 = getUrl_Bicycling(origin,dest)
                val fetchUrl3 = FetchUrl3("3")
                fetchUrl3.execute(url3)
*/
                //move map camera
                mMap.moveCamera(CameraUpdateFactory.newLatLng(origin))
                mMap.animateCamera(CameraUpdateFactory.zoomTo(12F))
            }
        }
    }

    // As TransportationAdapter, input: list & id (by one click), if we click any element on rv_transportation
    private fun implementLoad(list : List<TransportationDbO>) {
        adapterTransportation = TransportationAdapter(list,{
            id ->
            var url = getUrl(currentlocation, destlocation)
            var vehicle = "driving"
            when (id) {
                "driving" -> {
                }
                "walking" -> {
                    url = getUrl_Walking(currentlocation, destlocation)
                    vehicle = "walking"
                }
                "transit" -> {
                    url = getUrl_Transit(currentlocation, destlocation)
                    vehicle = "transit"
                }
            }
            //AddMarker(currentlocation,destlocation)
            Log.d("onMapClick: ", url)
            val fetchUrl = FetchUrl_ClickonRecycler(vehicle)
            Log.d("fetchUrl: ", fetchUrl.toString())
            // Start downloading json data from Google Directions API
            fetchUrl.execute(url)

            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentlocation))
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12F))
            //adapterTransportation.notifyDataSetChanged()
        })
        val layoutManager : RecyclerView.LayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rv_transportations.adapter = adapterTransportation
        rv_transportations.layoutManager = layoutManager
    }

    private fun AddMarker(currentlocation: LatLng, destlocation: LatLng){
        val edt_orgin = this.findViewById<View>(R.id.edt_orgin) as TextView
        val edt_destination = this.findViewById<View>(R.id.edt_destination) as TextView

        if (markerPoints.size > 1) {
            markerPoints.clear()
            mMap.clear()
        }

        //Add marker -> location
        markerPoints.add(currentlocation)
        markerPoints.add(destlocation)

        // Creating MarkerOptions
        val options = MarkerOptions()
        // Setting the position of the marker
        options.position(currentlocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        mMap.addMarker(options).title = edt_orgin.text.toString()
        options.position(destlocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        mMap.addMarker(options).title = edt_destination.text.toString()

        // Add new marker to the Google Map Android API V2
        mMap.addMarker(options)
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
        val url = "https://maps.googleapis.com/maps/api/directions/$output?$parameters&mode=driving&key=$apikey"

        return url
    }

    private fun getUrl_Walking(origin:LatLng, dest:LatLng):String {
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
        val url = "https://maps.googleapis.com/maps/api/directions/$output?$parameters&mode=walking&key=$apikey"

        return url
    }

    private fun getUrl_Transit(origin:LatLng, dest:LatLng):String {
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
        val url = "https://maps.googleapis.com/maps/api/directions/$output?$parameters&mode=transit&transit_mode=bus&key=$apikey"

        return url
    }

    //Step by step
    fun StepByStep(jObject: JSONObject) {

        //val routes = ArrayList<List<HashMap<String, String>>>()
        val jRoutes: JSONArray
        var jLegs: JSONArray
        var jSteps: JSONArray
        var jDuration: JSONObject
        var jDistance: JSONObject

        try {

            jRoutes = jObject.getJSONArray("routes")

            /** Traversing all routes  */
            for (i in 0 until jRoutes.length()) {
                jLegs = (jRoutes.get(i) as JSONObject).getJSONArray("legs")

                //val path = ArrayList<HashMap<String, String>>()

                jDuration = (jLegs.get(i) as JSONObject).getJSONObject("duration")
                Log.e("Step duration: ",jDuration.toString())

                jDistance = (jLegs.get(i) as JSONObject).getJSONObject("distance")
                Log.e("Step duration: ",jDistance.toString())

                /** Traversing all legs  */
                for (j in 0 until jLegs.length()) {
                    jSteps = (jLegs.get(j) as JSONObject).getJSONArray("steps")

                    /** Traversing all steps  */
                    var maneuver = ""
                    var instructions = ""
                    var distance = ""
                    var duration = ""
                    for (k in 0 until jSteps.length()) {
                        Log.e("Step", k.toString())
                        instructions = ((jSteps.get(k) as JSONObject).get("html_instructions")) as String
                        Log.e("Step instructions: ",  instructions)

                        maneuver = if(((jSteps.get(k) as JSONObject).has("maneuver")) ){
                            ((jSteps.get(k) as JSONObject).get("maneuver")) as String
                        } else "Head "
                        Log.e("Step maneuver: ", maneuver)

                        /*distance = ((jSteps.get(k) as JSONObject).get("distance") as JSONObject).get("text") as String
                        Log.e("Step distance: ", distance)

                        duration = ((jSteps.get(k) as JSONObject).get("duration") as JSONObject).get("text") as String
                        Log.e("Step duration: ", duration)*/

                    }
                }
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: Exception) {
        }
    }

    //Use an intent to launch the autocomplete activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        //autocompleteFragment.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val place = PlaceAutocomplete.getPlace(this, data)
                Log.e(TAG, "Place ID:" + place.id)
                val placeDB = PlaceData()
                placeDB.name = place.address.toString()
                /*placeDB.latitude = place.latLng.latitude
                placeDB.longitude = place.latLng.longitude
                placeDB.id = place.id*/

                edt_orgin.setText(placeDB.name)
                currentlocation = place.latLng

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                val status = PlaceAutocomplete.getStatus(this, data)
                Log.e(TAG, ""+status)
            } else if (resultCode == Activity.RESULT_CANCELED) {

            }
        } else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE2) {
            if (resultCode == Activity.RESULT_OK) {
                val place = PlaceAutocomplete.getPlace(this, data)
                Log.e(TAG, "Place ID:" + place.id)
                val placeDB = PlaceData()
                placeDB.name = place.address.toString()
                /*placeDB.latitude = place.latLng.latitude
                placeDB.longitude = place.latLng.longitude
                placeDB.id = place.id*/

                edt_destination.setText(placeDB.name)
                destlocation = place.latLng

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                val status = PlaceAutocomplete.getStatus(this, data)
                Log.e(TAG, ""+status)
            } else if (resultCode == Activity.RESULT_CANCELED) {

            }
        }
    }

    // Fetches data from url passed
    private inner class FetchUrl(transkind: String) :AsyncTask<String, Void, String>() {
         override fun doInBackground(vararg url:String):String {

             Log.d("FetchUrl doInBackground", "vô nè")

             // For storing data from web service
            var data = ""
            try
            {
                // Fetching the data from web service
                data = downloadUrl(url[0])

                Log.d("Background Task data", data)
            }
            catch (e:Exception) {
                Log.d("Background Task", e.toString())
            }
            return data
        }

        val transKind = transkind

        override fun onPostExecute(result:String) {
             Log.d("onPostExecue resute", result)
             super.onPostExecute(result)
             val parserTask = ParserTask(transKind,false,true)
            // Invokes the thread for parsing the JSON data
             parserTask.execute(result)
        }
    }

    private inner class FetchUrl_ClickonRecycler(transkind: String) :AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg url:String):String {

            Log.d("FetchUrl doInBackground", "vô nè")

            // For storing data from web service
            var data = ""
            try
            {
                // Fetching the data from web service
                data = downloadUrl(url[0])

                Log.d("Background Task data", data)
            }
            catch (e:Exception) {
                Log.d("Background Task", e.toString())
            }
            return data
        }

        val transKind = transkind

        override fun onPostExecute(result:String) {
            Log.d("onPostExecue resute", result)
            super.onPostExecute(result)
            val parserTask = ParserTask(transKind,true,true)
            // Invokes the thread for parsing the JSON data
            parserTask.execute(result)
        }
    }

    private inner class FetchUrl_NotPolyline(transkind: String) :AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg url:String):String {

            Log.d("FetchUrl doInBackground", "vô nè")

            // For storing data from web service
            var data = ""
            try
            {
                // Fetching the data from web service
                data = downloadUrl(url[0])

                Log.d("Background Task data", data)
            }
            catch (e:Exception) {
                Log.d("Background Task", e.toString())
            }
            return data
        }

        val transKind = transkind

        override fun onPostExecute(result:String) {
            Log.d("onPostExecue resute", result)
            super.onPostExecute(result)
            val parserTask = ParserTask(transKind,true,false)
            // Invokes the thread for parsing the JSON data
            parserTask.execute(result)
        }
    }
    /**
     * A method to download json data from url
     */
    @Throws(IOException::class)
    private fun downloadUrl(strUrl: String): String {
        Log.d("downloadUrl", "vô nè")
        var data = ""
        var iStream: InputStream? = null
        var urlConnection: HttpURLConnection? = null
        try {
            Log.d("downloadUrl try", "vô nè")
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

            /*val br = BufferedReader(InputStreamReader(iStream))
            //val br = BufferedReader(InputStreamReader(iStream!!))

            val sb = StringBuffer()
            var line = ""
            while(line !=null){
                line = br.readLine() //readLine() read data from file of BufferedReader
                sb.append(line)
            }

            data = sb.toString()
            Log.d("downloadUrl sb data= ", data.toString())*/
            //br.close()

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
    private inner class ParserTask(transkind: String, click_rv_trans: Boolean, draw_polyline: Boolean) : AsyncTask<String, Int, List<List<HashMap<String, String>>>>() {

        val transKind = transkind
        val click_rv_Trans = click_rv_trans
        val draw_Polyline = draw_polyline

        // Parsing the data in non-ui thread
        override fun doInBackground(vararg jsonData: String): List<List<HashMap<String, String>>>  {

            val jObject: JSONObject?
            try {
                jObject = JSONObject(jsonData[0])

                Log.d("ParserTask", jsonData[0])
                val parser = DataParser()
                StepByStep(jObject)
                Log.d("ParserTask", parser.toString())

                // Starts parsing data
                val routes: List<List<HashMap<String, String>>>  = parser.parse(jObject)

                //parse2: get duration of this all route
                val duration:  String  = parser.parse2(jObject)!!

                Log.d("ParaserTask", "Executing routes")
                Log.d("ParserTask", routes.toString())

                if(click_rv_Trans == false){
                    ///Add transportations's duration on rv_transportation
                    transList.clear()
                    when (transKind) {
                        "driving" -> {
                            transList.add(TransportationDbO("driving",duration))
                        }
                        "walking" -> {
                            transList.add(TransportationDbO("walking",duration))
                        }
                        "transit" -> {
                            transList.add(TransportationDbO("transit",duration))
                        }
                    }
                    //Error: Only the original thread that created a view hierarchy can touch its views.
                    //If it ain't runOnUiThread, this command know it's running on ParserTask(), not DirectionsActiivty
                    //But, adapterTransportation setted on DirectionsActivity, So this is solution
                    runOnUiThread { adapterTransportation.notifyDataSetChanged() }

                } else if(draw_Polyline == false){
                    when (transKind) {
                        "driving" -> {
                            transList.add(TransportationDbO("driving",duration))
                        }
                        "walking" -> {
                            transList.add(TransportationDbO("walking",duration))
                        }
                        "transit" -> {
                            transList.add(TransportationDbO("transit",duration))
                        }
                    }

                    //Error: Only the original thread that created a view hierarchy can touch its views.
                    //If it ain't runOnUiThread, this command know it's running on ParserTask(), not DirectionsActiivty
                    //But, adapterTransportation setted on DirectionsActivity, So this is solution
                    runOnUiThread { adapterTransportation.notifyDataSetChanged() }
                }

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
            if(draw_Polyline == true)
            {
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
                    mMap.clear()
                    AddMarker(currentlocation, destlocation)
                    mMap.addPolyline(lineOptions)
                } else {
                    Log.d("onPostExecute", "without Polylines drawn")
                }
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

    @Synchronized private fun buildGoogleApiClient() {
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
                        MY_PERMISSIONS_REQUEST_LOCATION)
            }
            else
            {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        MY_PERMISSIONS_REQUEST_LOCATION)
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
            MY_PERMISSIONS_REQUEST_LOCATION -> {
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
    }
    companion object {
        val MY_PERMISSIONS_REQUEST_LOCATION = 99
    }
}
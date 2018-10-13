package com.horus.travelweather.activity

import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.horus.travelweather.R
import java.io.IOException
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;
import com.horus.travelweather.model.PlaceDbO

import org.joda.time.DateTime;
import java.util.*
import java.util.concurrent.TimeUnit;

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private val overview = 0
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    //Getting current location
    private lateinit var lastLocation: Location

    private lateinit var start_location: String

    private lateinit var end_location: String
    /*
    // Receiving location updates - when u click anywhere, marker will move to there (RLU)
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false

    */
    //called when a marker is clicked or tapped
    //override fun onMarkerClick(p0: Marker?) = false


    //add a companion object with the code to request location permission
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        //private const val REQUEST_CHECK_SETTINGS = 2 // (RLU) is used as the request code passed to onActivityResult
    }
    private fun setUpMap() {
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
                val currentLatLng = LatLng(location.latitude, location.longitude)
                placeMarkerOnMap(currentLatLng) //to place marker above your current location
                //the zoom level of the map isn’t right, as it’s fully zoomed out.
                //zoom level of 12 is a nice in-between value that shows enough detail without getting crazy-close.
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14.5f)) //move to camera


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

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        start_location=""
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        /*
        //Update current location
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                //update current location with new location when u click others
                lastLocation = p0.lastLocation
                //then updating the maps with new location coordinates
                placeMarkerOnMap(LatLng(lastLocation.latitude, lastLocation.longitude))
            }
        }
        createLocationRequest() //Your app is now set to receive location update*/
    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        setupGoogleMapScreenSettings(googleMap)

        setUpMap()

        val place = intent.getSerializableExtra("MyPlace") as PlaceDbO
        end_location=place.name
        //Log.e("CurrentLocation : ",start_location)
        //Log.e("YourLocation : ",end_location)

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
                val currentLatLng = LatLng(location.latitude, location.longitude)
                placeMarkerOnMap(currentLatLng) //to place marker above your current location
                //the zoom level of the map isn’t right, as it’s fully zoomed out.
                //zoom level of 12 is a nice in-between value that shows enough detail without getting crazy-close.
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14.5f)) //move to camera


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
                        Log.e("start location11: ",addresses.get(0).getAddressLine(0))

                        start_location=addresses.get(0).getAddressLine(0)
                        Log.e("start location22: ","___________"+start_location)
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

        //val results = getDirectionsDetails(lastLocation.toString(), place.name, TravelMode.DRIVING)
        val results = getDirectionsDetails(start_location, end_location, TravelMode.DRIVING)
        if (results != null) {
            addPolyline(results, googleMap)
            positionCamera(results.routes[overview], googleMap)
            addMarkersToMap(results, googleMap)
        }

        //Here you enable the zoom controls on the map and declare MapsActivity as the callback triggered when the user clicks a marker on this map
        /*mMap.getUiSettings().setZoomControlsEnabled(true)
        mMap.setOnMarkerClickListener(this)

        setUpMap()*/

    }

    //Using DirectionAPI class to get direction from original location to destinational location
    //TravelMode just support: walking, driving and bicycling directions
    private fun getDirectionsDetails(origin:String, destination:String, mode:TravelMode): DirectionsResult? {
        val now = DateTime()
        try
        {
            return DirectionsApi.newRequest(getGeoContext())
                    .mode(mode)
                    .origin(origin)
                    .destination(destination)
                    .departureTime(now)
                    .await()     //making a synchronous call to the web service and return us a DirectionsResult object.
        }
        catch (e: ApiException) {
            e.printStackTrace()
            return null
        }
        catch (e:InterruptedException) {
            e.printStackTrace()
            return null
        }
        catch (e:IOException) {
            e.printStackTrace()
            return null
        }
    }

    private fun setupGoogleMapScreenSettings(mMap:GoogleMap) {
        mMap.setBuildingsEnabled(true)               //Turns the 3D buildings layer on
        mMap.setIndoorEnabled(true)                  //Sets whether indoor maps should be enabled.
        mMap.setTrafficEnabled(true)                 //Turns the traffic layer on or off.
        val mUiSettings = mMap.getUiSettings()
        mUiSettings.setZoomControlsEnabled(true)     //it can be zoom control
        mUiSettings.setCompassEnabled(true)          //....compass (la ban)
        mUiSettings.setMyLocationButtonEnabled(true) //Enables or disables the my-location layer.
        mUiSettings.setScrollGesturesEnabled(true)   //....cử chỉ scroll
        mUiSettings.setZoomGesturesEnabled(true)     //...zoom
        mUiSettings.setTiltGesturesEnabled(true)     //...Tilt (nghiêng)
        mUiSettings.setRotateGesturesEnabled(true)   //...Rotate
    }

    //Add marker & display address title for starlocation and endlocation
    //A route is array of DirectionsRoute[], each element includes original location & destinational location coordinates/address/placeid
    //A leg is array of DirectionsLeg[] each element includes that coordinate info (latitude, longitude, duration, distance...)
    private fun addMarkersToMap(results:DirectionsResult, mMap:GoogleMap) {
        mMap.addMarker(MarkerOptions().position(LatLng(results.routes[overview].legs[overview].startLocation.lat, results.routes[overview].legs[overview].startLocation.lng)).title(results.routes[overview].legs[overview].startAddress))
        mMap.addMarker(MarkerOptions().position(LatLng(results.routes[overview].legs[overview].endLocation.lat, results.routes[overview].legs[overview].endLocation.lng)).title(results.routes[overview].legs[overview].startAddress).snippet(getEndLocationTitle(results)))
    }
    //moveCamera by each leg
    //the zoom level of the map isn’t right, as it’s fully zoomed out.
    //zoom level of 12 is a nice in-between value that shows enough detail without getting crazy-close.
    private fun positionCamera(route:DirectionsRoute, mMap:GoogleMap) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(route.legs[overview].startLocation.lat, route.legs[overview].startLocation.lng), 12f))
    }
    //Polyline by routes (not legs)
    //A polyline is a list of points, where line segments are drawn between consecutive points
    //(tức các phân đoạn đường đc vẽ trên maps (có thể set width, color.....))
    private fun addPolyline(results:DirectionsResult, mMap:GoogleMap) {
        val decodedPath = PolyUtil.decode(results.routes[overview].overviewPolyline.getEncodedPath())
        mMap.addPolyline(PolylineOptions().addAll(decodedPath))
    }
    //Show distance & duration on destinational location by leg
    private fun getEndLocationTitle(results:DirectionsResult):String {
        return "Time :" + results.routes[overview].legs[overview].duration.humanReadable + " Distance :" + results.routes[overview].legs[overview].distance.humanReadable
    }

    // Before using directionsAPI class we will need to create a GeoApiContext object
    // (This object is where we will set our API key and some restrictions (han che))
    // to request direction information. This newRequest method takes a GeoApiContext
    // as an argument, which then returns us a DirectionsApiRequest.
    private fun getGeoContext():GeoApiContext {
        val geoApiContext = GeoApiContext()
        return geoApiContext
                .setQueryRateLimit(3)        // The maximum number of queries that will be executed during a 1 second intervals.
                .setApiKey(getString(R.string.directionsApiKey))
                .setConnectTimeout(1, TimeUnit.SECONDS)  // The default connect timeout for new connections.
                .setReadTimeout(1, TimeUnit.SECONDS)     // The default read timeout for new connections.
                .setWriteTimeout(1, TimeUnit.SECONDS)    // The default write timeout for new connections.
    }



    ///// Receiving Location Updates (updating current location when u click others)
    // 1
    // Override AppCompatActivity’s onActivityResult() method and start the update request if it has a RESULT_OK result for a REQUEST_CHECK_SETTINGS request.
    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
    }
    // 2
    // Override onPause() to stop location update request
    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
    // 3
    // Override onResume() to restart the location update request.
    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }
    //4
    private fun createLocationRequest() {
        // 1
        locationRequest = LocationRequest()
        // 2
        locationRequest.interval = 10000
        // 3
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

        // 4
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        // 5
        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            // 6
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(this@MapsActivity,
                            REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }
    private fun startLocationUpdates() {
        //In startLocationUpdates(), if the ACCESS_FINE_LOCATION permission has not been granted, request it now and return.
        // If there is permission, request for location updates.
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        //2
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null *//* Looper *//*)
    }*/

    //to place marker above your current location (default: <marker color: red, shape: drop of water>)
    private fun placeMarkerOnMap(location: LatLng) {
        // 1
        val markerOptions = MarkerOptions().position(location)
        //Get current location address to show above marker
        //val titleStr = getAddress(location)
        //markerOptions.title(titleStr)
        //but We can set your marker with different shape - current location
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(
                BitmapFactory.decodeResource(resources, R.mipmap.ic_user_location)))
        // 2
        mMap.addMarker(markerOptions)

    }
    /*
    //Show address above marker by Geocoder class
    private fun getAddress(latLng: LatLng): String {
        // 1
        val geocoder = Geocoder(this)
        val addresses: List<Address>?
        val address: Address?
        var addressText = ""

        try {
            // 2
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            // 3
            if (null != addresses && !addresses.isEmpty()) {
                address = addresses[0]
                for (i in 0 until address.maxAddressLineIndex) {
                    addressText += if (i == 0) address.getAddressLine(i) else "\n" + address.getAddressLine(i)
                }
            }
        } catch (e: IOException) {
            Log.e("MapsActivity", e.localizedMessage)
        }

        return addressText
    }*/



}

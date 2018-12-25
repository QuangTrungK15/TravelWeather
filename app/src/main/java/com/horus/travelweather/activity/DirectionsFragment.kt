package com.horus.travelweather.activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PointF
import android.location.Location
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSmoothScroller
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.RelativeLayout
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
import com.horus.travelweather.adapter.StepbyStepDirectionsAdapter
import com.horus.travelweather.adapter.TransportationAdapter
import com.horus.travelweather.database.PlaceEntity
import com.horus.travelweather.model.DirectionsStepDbO
import com.horus.travelweather.model.TransitDbO
import com.horus.travelweather.model.TransportationDbO
import kotlinx.android.synthetic.main.activity_directions.*
import kotlinx.android.synthetic.main.activity_directions.view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList


class DirectionsFragment : Fragment(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private val TAG = DirectionsFragment::class.java.simpleName
    private lateinit var adapterTransportation : TransportationAdapter
    private lateinit var adapterStepbyStepDirections : StepbyStepDirectionsAdapter
    var PLACE_AUTOCOMPLETE_REQUEST_CODE = 1
    var PLACE_AUTOCOMPLETE_REQUEST_CODE2 = 2

    private lateinit var mMap:GoogleMap
    private lateinit var markerPoints:ArrayList<LatLng>
    private lateinit var mGoogleApiClient:GoogleApiClient
    private lateinit var mLastLocation:Location
    private var mCurrLocationMarker: Marker? = null
    private lateinit var mLocationRequest:LocationRequest

    var transList = ArrayList<TransportationDbO>()
    var stepsList = ArrayList<DirectionsStepDbO>()
    var stepsList_temp = ArrayList<DirectionsStepDbO>()

    private var count: Int = 1


    var currentlocation = LatLng(10.762622, 106.660172)
    var destlocation = LatLng(10.762622, 106.660172)

    private lateinit var textToSpeech: TextToSpeech


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_directions, container, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkLocationPermission()
        }

        setHasOptionsMenu(true)

        // Initializing
        markerPoints = ArrayList<LatLng>()
        //
        textToSpeech = TextToSpeech(context!!, TextToSpeech.OnInitListener { i ->
            if (i == TextToSpeech.SUCCESS) {
                //result = textToSpeech.setLanguage(Locale.UK)
                textToSpeech.speak("", TextToSpeech.QUEUE_FLUSH, null)
            } else {
                Toast.makeText(context!!,"Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show()
            }
        })

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        if(getFragmentManager() != null)
        {
            val mMapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
//            val mapFragment = getFragmentManager()!!
//                    .findFragmentById(R.id.map) as SupportMapFragment
            mMapFragment.getMapAsync(this)
        }

//        val edt_orgin = this.findViewById<View>(R.id.edt_orgin) as TextView
//        val edt_destination = this.findViewById<View>(R.id.edt_destination) as TextView
//        //This btn shows direction (org -> dest) steps
//        val btn_steps = this.findViewById<View>(R.id.btn_steps) as Button

        //val recyclerView = this.findViewById<View>(R.id.rv_directionsSteps) as RecyclerView

//        val behavior = BottomSheetBehavior.from(recyclerView)

        //val transList = ArrayList<TransportationDbO>()
        transList.add(TransportationDbO("driving",""))
        transList.add(TransportationDbO("walking",""))
        transList.add(TransportationDbO("transit",""))
        implementLoad(transList,view.rv_transportations)

        stepsList.add(DirectionsStepDbO(0,"","","","","",
                currentlocation, TransitDbO("","","","","","","")))
        loadingStepbyStep(stepsList,view.rv_directionsSteps)

        view.clicksteps.setOnClickListener{
            numberofclick++

            /*if(numberofclick == 1){
                layoutParams_temp = scroll_directionsdetail.layoutParams as RelativeLayout.LayoutParams

                Log.e("ACTION stop=: ",layoutParams_temp!!.topMargin.toString())
                numberofclick++
            }*/

            if(count%2 != 0)
            {
                view.rv_directionsSteps.visibility = View.VISIBLE

                view.linear_orgindest.animate()
                        .translationY(linear_orgindest.height.toFloat())
                        .alpha(0.0f)
                        .duration = 800
                view.rv_transportations.animate()
                        .translationY(rv_transportations.height.toFloat())
                        .alpha(0.0f)
                        .duration = 800

                view.clicksteps.setOnTouchListener(onTouchListener())

                view.linear_orgindest.visibility = View.GONE
                view.rv_transportations.visibility = View.GONE
            }
            else{
                view.rv_directionsSteps.visibility = View.GONE

                view.linear_orgindest.animate()
                        .translationY(0F)
                        .alpha(1.0f)
                        .duration = 200
                view.rv_transportations.animate()
                        .translationY(0F)
                        .alpha(1.0f)
                        .duration = 200

                view.linear_orgindest.visibility = View.VISIBLE
                view.rv_transportations.visibility = View.VISIBLE
            }
            count++
        }
        /*stepsicon.setOnClickListener{
            if(count%2 != 0)
            {
                rv_directionsSteps.visibility = View.VISIBLE
                linear_orgindest.visibility = View.GONE
                rv_transportations.visibility = View.GONE
            }
            else{
                rv_directionsSteps.visibility = View.GONE
                linear_orgindest.visibility = View.VISIBLE
                rv_transportations.visibility = View.VISIBLE
            }
            count++
        }*/

        view.btn_previous.setOnClickListener{
            if(thestep > 0 && thestep < stepsList.size){
                thestep--
                pre_nextstep(thestep)
            }
        }

        view.btn_next.setOnClickListener{
            if(thestep >= 0 && thestep < stepsList.size - 1){
                thestep++
                pre_nextstep(thestep)
            }
        }

        view.imgbtn_updown.setOnClickListener{
            val edt_origin_temp = view.edt_orgin.text
            view.edt_orgin.text = edt_destination.text
            view.edt_destination.text = edt_origin_temp

            val currentlocation_temp = currentlocation
            currentlocation = destlocation
            destlocation = currentlocation_temp
        }

        view.edt_orgin.setOnClickListener {
            //Filter results by place type (by address: get full address, by establisment: get business address)
            val typeFilter = AutocompleteFilter.Builder()
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT)
                    .build()
            //Use an intent to launch the autocomplete activity (fullscreen mode)
            //https://developers.google.com/places/android-sdk/autocomplete
            val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                    .setFilter(typeFilter)
                    .build(activity)
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
        }
        view.edt_destination.setOnClickListener {
            //Filter results by place type (by address: get full address, by establisment: get business address)
            val typeFilter = AutocompleteFilter.Builder()
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT)
                    .build()
            //Use an intent to launch the autocomplete activity (fullscreen mode)
            //https://developers.google.com/places/android-sdk/autocomplete
            val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                    .setFilter(typeFilter)
                    .build(activity)
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE2)
        }
        return view
    }



//    @SuppressLint("ClickableViewAccessibility")
//    @RequiresApi(Build.VERSION_CODES.M)
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_directions)
        //val actionBar = actionBar
        //actionBar.hide()

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
//        {
//            checkLocationPermission()
//        }
//        // Initializing
//        markerPoints = ArrayList<LatLng>()
//        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        val mapFragment = getFragmentManager()!!
//                .findFragmentById(R.id.map) as SupportMapFragment
//        mapFragment.getMapAsync(this)
//
//        val edt_orgin = this.findViewById<View>(R.id.edt_orgin) as TextView
//        val edt_destination = this.findViewById<View>(R.id.edt_destination) as TextView
//        //This btn shows direction (org -> dest) steps
//        val btn_steps = this.findViewById<View>(R.id.btn_steps) as Button
//
//        //val recyclerView = this.findViewById<View>(R.id.rv_directionsSteps) as RecyclerView
//
////        val behavior = BottomSheetBehavior.from(recyclerView)
//
//        //val transList = ArrayList<TransportationDbO>()
//        transList.add(TransportationDbO("driving",""))
//        transList.add(TransportationDbO("walking",""))
//        transList.add(TransportationDbO("transit",""))
//        implementLoad(transList)
//
//        stepsList.add(DirectionsStepDbO(0,"head","s","s","3243","432", currentlocation))
//        loadingStepbyStep(stepsList)
//
//        clicksteps.setOnClickListener{
//            numberofclick++
//
//            /*if(numberofclick == 1){
//                layoutParams_temp = scroll_directionsdetail.layoutParams as RelativeLayout.LayoutParams
//
//                Log.e("ACTION stop=: ",layoutParams_temp!!.topMargin.toString())
//                numberofclick++
//            }*/
//
//            if(count%2 != 0)
//            {
//                rv_directionsSteps.visibility = View.VISIBLE
//
//                linear_orgindest.animate()
//                        .translationY(linear_orgindest.height.toFloat())
//                        .alpha(0.0f)
//                        .duration = 800
//                rv_transportations.animate()
//                        .translationY(rv_transportations.height.toFloat())
//                        .alpha(0.0f)
//                        .duration = 800
//
//                clicksteps.setOnTouchListener(onTouchListener())
//
//                linear_orgindest.visibility = View.GONE
//                rv_transportations.visibility = View.GONE
//            }
//            else{
//                rv_directionsSteps.visibility = View.GONE
//
//                linear_orgindest.animate()
//                        .translationY(0F)
//                        .alpha(1.0f)
//                        .duration = 200
//                rv_transportations.animate()
//                        .translationY(0F)
//                        .alpha(1.0f)
//                        .duration = 200
//
//                linear_orgindest.visibility = View.VISIBLE
//                rv_transportations.visibility = View.VISIBLE
//            }
//            count++
//        }
//        /*stepsicon.setOnClickListener{
//            if(count%2 != 0)
//            {
//                rv_directionsSteps.visibility = View.VISIBLE
//                linear_orgindest.visibility = View.GONE
//                rv_transportations.visibility = View.GONE
//            }
//            else{
//                rv_directionsSteps.visibility = View.GONE
//                linear_orgindest.visibility = View.VISIBLE
//                rv_transportations.visibility = View.VISIBLE
//            }
//            count++
//        }*/
//
//
//
//        edt_orgin.setOnClickListener {
//            //Filter results by place type (by address: get full address, by establisment: get business address)
//            val typeFilter = AutocompleteFilter.Builder()
//                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
//                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT)
//                    .build()
//            //Use an intent to launch the autocomplete activity (fullscreen mode)
//            //https://developers.google.com/places/android-sdk/autocomplete
//            val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
//                    .setFilter(typeFilter)
//                    .build(this)
//            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
//        }
//        edt_destination.setOnClickListener {
//            //Filter results by place type (by address: get full address, by establisment: get business address)
//            val typeFilter = AutocompleteFilter.Builder()
//                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
//                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT)
//                    .build()
//            //Use an intent to launch the autocomplete activity (fullscreen mode)
//            //https://developers.google.com/places/android-sdk/autocomplete
//            val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
//                    .setFilter(typeFilter)
//                    .build(this)
//            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE2)
//        }
//    }

    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        val distanceInPx = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        return pxToDp(distanceInPx)
    }

    private fun pxToDp(px: Float): Float {
        return px / resources.displayMetrics.density
    }
    //MotionEvent.ACTION_UP means you touch sensor fist time
    //MotionEvent.ACTION_DOWN means that touch sensor does not detect anymore
    //for example MotionEvent.ACTION_MOVE means that sensor detects some moves (like swipe, scroll etc.)
    //When u move clicksteps (that top bar above direction steps list)
    private var xDelta:Int = 0
    private var yDelta:Int = 0
    //private val CLICK_ACTION_THRESHHOLD = 200

    //to determind click event when touchevent is running
    private val MAX_CLICK_DURATION = 400
    private val MAX_CLICK_DISTANCE = 5
    private var startClickTime: Long = 0
    private var stayedWithinClickDistance:Boolean = false
    private var x1: Float = 0.toFloat()
    private var y1: Float = 0.toFloat()
    private var dx: Float = 0.toFloat()
    private var dy: Float = 0.toFloat()
    private var firstclick:Boolean = false
    private var numberofclick:Int = 0
    var layoutParams_temp: RelativeLayout.LayoutParams? = null

    private fun onTouchListener(): View.OnTouchListener {
        return View.OnTouchListener { view, event ->
//            val mainLayout = findViewById<View>(R.id.clicksteps) as RelativeLayout
            val x = event.rawX.toInt()
            val y = event.rawY.toInt()
            val lParams = view.layoutParams as RelativeLayout.LayoutParams

            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    if(clicksteps.bottom != directionsID.bottom) {

                        xDelta = x - lParams.leftMargin
                        yDelta = y - lParams.topMargin
                        //to determind click event
                        x1 = event.x
                        y1 = event.y
                        startClickTime = System.currentTimeMillis()
                        stayedWithinClickDistance = true
                        firstclick = true
                    }
                }

                MotionEvent.ACTION_UP -> {
                    //if(clicksteps.top != directionsID.top) {

                      //  yDelta = y + lParams.topMargin

                        val clickDuration = Calendar.getInstance().timeInMillis - startClickTime
                        dx = event.x - x1
                        dy = event.y - y1
                        if (clickDuration < MAX_CLICK_DURATION && dx < MAX_CLICK_DISTANCE && dy < MAX_CLICK_DISTANCE) {
                            Log.v("", "On Item Clicked:: ")
                            view.performClick()
                        }

                       // firstclick = true
                    //}
                    /* if (pressDuration < MAX_CLICK_DURATION && stayedWithinClickDistance) {
                         // Click event has occurred
                         view.performClick()
                     }*/

                }

                MotionEvent.ACTION_MOVE -> {

                    val layoutParams = view.layoutParams as RelativeLayout.LayoutParams

                    //if(clicksteps.bottom != directionsID.bottom){
                    if (firstclick == true) {
                        layoutParams.topMargin = y - yDelta
                        layoutParams.rightMargin = 0
                        layoutParams.bottomMargin = 0
                        //layoutParams_temp = view.layoutParams as RelativeLayout.LayoutParams
                        view.layoutParams = layoutParams

                        Log.e("topmargin: ", (directionsID.bottom).toString())
                        Log.e("topmargin2: ", (clicksteps.bottom).toString())
                        firstclick = false
                    }
                    //}
                    /*else if (clicksteps.bottom == directionsID.bottom){

                        Log.e("ACTION =: ","false")

                        *//*val row = findViewById<View>(R.id.scroll_directionsdetail) as RelativeLayout
                        val rv_directionsSteps_temp = findViewById<View>(R.id.rv_directionsSteps) as RecyclerView
                        val clicksteps_temp = findViewById<View>(R.id.clicksteps) as RelativeLayout

                        row.removeView(clicksteps_temp)
                        row.removeView(rv_directionsSteps_temp)
                        row.addView(clicksteps_temp)
                        row.addView(rv_directionsSteps_temp)
                        row.invalidate()*//*



                        val params = RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.MATCH_PARENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT)
                        params.addRule(RelativeLayout.VISIBLE)
                        params.addRule(RelativeLayout.ALIGN_BOTTOM,directionsID.bottom)
                        //clicksteps.top = 900
                        //params.addRule(RelativeLayout.BELOW, R.id.clicksteps)

                        rv_directionsSteps.layoutParams = params

                        Log.e("ACTION stop1=: ",clicksteps.top.toString())
                        Log.e("ACTION stop3=: ",rv_directionsSteps.top.toString())

                        Log.e("ACTION stop2=: ",rv_directionsSteps.top.toString())
                        Log.e("ACTION stop4=: ",rv_directionsSteps.bottom.toString())


                        //nhan dang touch len de update chạy code nhu tren, sau do van check tiep de han che dung thanh bottom
                   }*/
                    stayedWithinClickDistance = false

                }

            }
            // Because we call this from onTouchEvent, this code will be executed for both
            // normal touch events and for when the system calls this using Accessibility
            clicksteps.invalidate()
            true
        }
    }

    override fun onMapReady(googleMap:GoogleMap) {
        mMap = googleMap

        setupGoogleMapScreenSettings(googleMap)

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if ((ContextCompat.checkSelfPermission(context!!,
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
//        val fab_directions = this.findViewById<View>(R.id.fab_directions) as FloatingActionButton
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

            val fetchUrl_present = FetchUrl_ClickonRecycler("driving")
            fetchUrl_present.execute(url)


            //move map camera
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentlocation))
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12F))
        }

        //When myuser click anywhere on maps
        /*mMap.setOnMapClickListener { point ->
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
            *//**
             * For the start location, the color of marker is GREEN and
             * for the end location, the color of marker is RED.
             *//*
            *//**
             * For the start location, the color of marker is GREEN and
             * for the end location, the color of marker is RED7.
             *//*
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

                *//*val url2 = getUrl_Walking(origin,dest)
                val fetchUrl2 = FetchUrl2("2")
                fetchUrl2.execute(url2)

                val url3 = getUrl_Bicycling(origin,dest)
                val fetchUrl3 = FetchUrl3("3")
                fetchUrl3.execute(url3)
*//*
                //move map camera
                mMap.moveCamera(CameraUpdateFactory.newLatLng(origin))
                mMap.animateCamera(CameraUpdateFactory.zoomTo(12F))
            }
        }*/
    }

    // As TransportationAdapter, input: list & id (by one click), if we click any element on rv_transportation
    private fun implementLoad(list : List<TransportationDbO>,rv_transportations : RecyclerView) {
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
        val layoutManager : RecyclerView.LayoutManager = LinearLayoutManager(context!!, LinearLayoutManager.HORIZONTAL, false)
        rv_transportations.adapter = adapterTransportation
        rv_transportations.layoutManager = layoutManager
        //rv_directionsSteps.setHasFixedSize(true)
    }

    var thestep = 0 //step of rv_directionsteps

    private fun loadingStepbyStep(list : List<DirectionsStepDbO>,rv_directionsSteps : RecyclerView) {
        adapterStepbyStepDirections = StepbyStepDirectionsAdapter(list,{
            id ->

            pre_nextstep(id.toInt())
            //adapterStepbyStepDirections.notifyDataSetChanged()

        })
        val layoutManager2 : RecyclerView.LayoutManager = SmoothLinearLayoutManager(context!!, LinearLayoutManager.VERTICAL, false)
        rv_directionsSteps.adapter = adapterStepbyStepDirections
        rv_directionsSteps.layoutManager = layoutManager2

    }

    var options_flat: Boolean = false
    private var markerName_temp: Marker? = null

    private fun pre_nextstep(id: Int){
        thestep = id.toInt()

        //val toolbar = this.findViewById<View>(R.id.toolbar) as Toolbar?

        //setSupportActionBar(toolbar)

        goto_stepbystep.visibility = View.VISIBLE
        show_pre_next.visibility = View.VISIBLE

        scroll_directionsdetail.visibility = View.GONE
        linear_orgindest.visibility = View.GONE
        rv_transportations.visibility = View.GONE
        fab_directions.visibility = View.GONE



        //val imgView_goto_direction = this.findViewById<View>(R.id.imgView_goto_direction) as AppCompatImageView
        //val tv_goto_distance9 = this.findViewById<View>(R.id.tv_goto_distance9) as TextView
        //val tv_goto_instructions = this.findViewById<View>(R.id.tv_goto_instructions) as TextView

        tv_goto_distance9.text = stepsList[id.toInt()].distance
        tv_goto_instructions.text = stepsList[id.toInt()].instructions

        if(stepsList[id.toInt()].direction == "Head" || stepsList[id.toInt()].direction == "Straight"){
            imgView_goto_direction.setImageResource(R.drawable.ic24_head)
        } else if(stepsList[id.toInt()].direction == "turn-left"){
            imgView_goto_direction.setImageResource(R.drawable.ic24_turnleft)
        } else if(stepsList[id.toInt()].direction == "turn-right"){
            imgView_goto_direction.setImageResource(R.drawable.ic24_turnright)
        } else if(stepsList[id.toInt()].direction == "turn-slight-right"){ //chếch sang phải
            imgView_goto_direction.setImageResource(R.drawable.ic24_turnslightright)
        } else if(stepsList[id.toInt()].direction == "turn-slight-left"){
            imgView_goto_direction.setImageResource(R.drawable.ic24_turnslightleft)
        }else if(stepsList[id.toInt()].direction == "turn-sharp-right"){ // ngoặc phải
            imgView_goto_direction.setImageResource(R.drawable.ic24_turnsharpright)
        } else if(stepsList[id.toInt()].direction == "turn-sharp-left"){
            imgView_goto_direction.setImageResource(R.drawable.ic24_turnsharpleft)
        } else if(stepsList[id.toInt()].direction == "ferry"){
            imgView_goto_direction.setImageResource(R.drawable.ic24_ferry)
        } else if(stepsList[id.toInt()].direction == "ferry-train"){
            imgView_goto_direction.setImageResource(R.drawable.ic24_ferry)
        } else if(stepsList[id.toInt()].direction == "ramp-right"){ //tại nút giao thông
            imgView_goto_direction.setImageResource(R.drawable.ic24_rampleft)
        } else if(stepsList[id.toInt()].direction == "ramp-left"){
            imgView_goto_direction.setImageResource(R.drawable.ic24_rampleft)
        } else if(stepsList[id.toInt()].direction == "fork-right"){ //tại nút giao thông
            imgView_goto_direction.setImageResource(R.drawable.ic24_rampleft)
        } else if(stepsList[id.toInt()].direction == "fork-left"){
            imgView_goto_direction.setImageResource(R.drawable.ic24_rampleft)
        } else if(stepsList[id.toInt()].direction == "uturn-right"){
            imgView_goto_direction.setImageResource(R.drawable.ic24_uturnright)
        } else if(stepsList[id.toInt()].direction == "uturn-left"){
            imgView_goto_direction.setImageResource(R.drawable.ic24_uturnleft)
        } else if(stepsList[id.toInt()].direction == "merge"){
            imgView_goto_direction.setImageResource(R.drawable.ic24_merge)
        } else if(stepsList[id.toInt()].direction == "roundabout-right"){
            imgView_goto_direction.setImageResource(R.drawable.ic24_roundabout)
        } else if(stepsList[id.toInt()].direction == "roundabout-left"){
            imgView_goto_direction.setImageResource(R.drawable.ic24_roundabout)
        } else if(stepsList[id.toInt()].direction == "keep-right"){
            imgView_goto_direction.setImageResource(R.drawable.ic24_keepright)
        } else if(stepsList[id.toInt()].direction == "keep-left"){
            imgView_goto_direction.setImageResource(R.drawable.ic24_keepleft)
        } else{
            imgView_goto_direction.setImageResource(R.drawable.ic24_head)
        }
        val actionBar1 = (activity as AppCompatActivity).supportActionBar

        if (actionBar1 != null) {
            actionBar1.setDisplayHomeAsUpEnabled(true)
            actionBar1.title = "Xem trước tuyến đường"
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLng(stepsList[id.toInt()].latLng))
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18F))

        // Setting the position of the marker
        //val options = MarkerOptions()
        if(markerName_temp != null) markerName_temp!!.remove()

        val markerName = mMap.addMarker(MarkerOptions().position(stepsList[thestep].latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))
        //options.position(stepsList[id.toInt()].latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
        //options.flat(true)
        markerName_temp = markerName
        // Add new marker to the Google Map Android API V2
        //mMap.addMarker(options)

        //options_flat = true

        if((if (textToSpeech != null) textToSpeech else throw NullPointerException("Expression 'textToSpeech' must not be null")).isSpeaking){
            textToSpeech.shutdown()
        }

        textToSpeech = TextToSpeech(context!!, TextToSpeech.OnInitListener { i ->
            if (i == TextToSpeech.SUCCESS) {
                //result = textToSpeech.setLanguage(Locale.UK)
                textToSpeech.speak(tv_goto_instructions.text.toString(), TextToSpeech.QUEUE_FLUSH, null)
            } else {
                Toast.makeText(context!!,"Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show()
            }
        })

    }


    override fun onOptionsItemSelected(item: MenuItem?):Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                // todo: goto back activity from here

                val actionBar1 = (activity as AppCompatActivity).supportActionBar
                actionBar1!!.title = "Direction"
                actionBar1.setDisplayHomeAsUpEnabled(false)
                /*if (actionBar1 != null) {
                    actionBar1.hide()
                }*/

                goto_stepbystep.visibility = View.GONE
                show_pre_next.visibility = View.GONE
                linear_orgindest.visibility = View.VISIBLE
                rv_transportations.visibility = View.VISIBLE
                scroll_directionsdetail.visibility = View.VISIBLE
                fab_directions.visibility = View.VISIBLE

                //mMap.animateCamera(CameraUpdateFactory.zoomTo(12F))
                thestep = 0

                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun AddMarker(currentlocation: LatLng, destlocation: LatLng){
//        val edt_orgin = this.findViewById<View>(R.id.edt_orgin) as TextView
//        val edt_destination = this.findViewById<View>(R.id.edt_destination) as TextView

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
        val url = "https://maps.googleapis.com/maps/api/directions/$output?$parameters&language=vi&mode=driving&key=$apikey"

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
        val url = "https://maps.googleapis.com/maps/api/directions/$output?$parameters&language=vi&mode=walking&key=$apikey"

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
        val url = "https://maps.googleapis.com/maps/api/directions/$output?$parameters&language=vi&mode=transit&transit_mode=bus&key=$apikey"

        return url
    }

    @RequiresApi(Build.VERSION_CODES.M)
//Step by step
    fun StepByStep(jObject: JSONObject) {

        //val routes = ArrayList<List<HashMap<String, String>>>()
        val jRoutes: JSONArray
        var jLegs: JSONArray
        var jSteps: JSONArray
        var jDuration: JSONObject
        var jDistance: JSONObject
        var maneuver = ""
        var instructions = ""
        var distance = ""
        var duration = ""

        //Coordinator of each step
        var start_location_lat = ""
        var start_location_lng = ""
        var count = 0

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

                    stepsList.clear()
                    count = 0
                    for (k in 0 until jSteps.length()) {

                        maneuver = if(((jSteps.get(k) as JSONObject).has("maneuver")) ){
                            ((jSteps.get(k) as JSONObject).get("maneuver")) as String
                        } else "Head"
                        Log.e("Step maneuver: ", maneuver)

                        instructions = ((jSteps.get(k) as JSONObject).get("html_instructions")) as String

                        Log.e("Step-0 instructions: ",  instructions)

                        //Divide instructions string to instruction9 & attention
                        var instructions9 = instructions
                        var attention = ""           // just additional info about that step
                        var flag = false
                        var j = 0
                        var end = instructions.length - 1

                        while (j <= end){
                            //
                            if(instructions[j] == '<' && instructions[j+1] == 'd'){
                                attention = instructions.substring(j,end+1)
                                instructions9 = instructions.substring(0,j)
                                Log.e("Step1 instructions9: ",  instructions9)
                                Log.e("Step1 attention: ",  attention)
                                break
                            }
                            j++
                        }

                        Log.e("Step1 instructions9: ",  instructions9)
                        Log.e("Step1 attention: ",  attention)


                        var start = 0
                        var i = 0
                        while(i < instructions9.length){

                            if (instructions9[i] == '<') { start = i }

                            if(instructions9[i] == '&' && instructions9[i+1] == 'a' && instructions9[i+2] == 'm'
                                    && instructions9[i+3] == 'p' && instructions9[i+4] == ';')
                            {
                                val first = instructions9.substring(0,i+1)
                                val last = instructions9.substring(i+5,instructions9.length)
                                instructions9 = first + last
                            }

                            if (instructions9[i] == '>')
                            {
                                val first = instructions9.substring(0,start)
                                val last = instructions9.substring(i+1)
                                val newins = first + last

                                instructions9 = newins

                                i=0
                                start = 0
                            }
                            i++
                        }


                        var start2 = 0
                        var i2 = 0
                        while(i2 < attention.length){
                            if (attention[i2] == '&' || attention[i2] == '<') { start2 = i2 }
                            if (attention[i2] == ';' || attention[i2] == '>')
                            {
                                var spot = ""

                                if(attention[i2 - 1] == 'v') spot = ". "

                                val first = attention.substring(0,start2)
                                val last = attention.substring(i2+1)
                                val newins = first + spot + last

                                Log.e("Step last: ",  last.toString())

                                attention = newins

                                i2=0
                                Log.e("Step ins: ",  i2.toString())

                                start2 = 0
                            }
                            i2++
                        }

                        Log.e("Step instructions: ",  instructions9)

                        distance = ((jSteps.get(k) as JSONObject).get("distance") as JSONObject).get("text") as String
                        Log.e("Step distance: ", distance)

                        duration = ((jSteps.get(k) as JSONObject).get("duration") as JSONObject).get("text") as String
                        Log.e("Step duration: ", duration)

                        start_location_lat = (((jSteps.get(k) as JSONObject).get("start_location") as JSONObject).get("lat") as Double).toString()
                        Log.e("Step duration: ", start_location_lat)

                        start_location_lng = (((jSteps.get(k) as JSONObject).get("start_location") as JSONObject).get("lng") as Double).toString()
                        Log.e("Step duration: ", start_location_lng)

                        Log.e("Step duration2: ", LatLng(start_location_lat.toDouble(),start_location_lng.toDouble()).toString())

                        stepsList.add(DirectionsStepDbO(count++,maneuver,instructions9,attention,duration,distance,
                                LatLng(start_location_lat.toDouble(),start_location_lng.toDouble()),
                                TransitDbO("","","","","","","")))
                        getActivity()!!.runOnUiThread { adapterStepbyStepDirections.notifyDataSetChanged() }

                    }
                }
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: Exception) {
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
//Step by step
    fun StepByStep_Transit(jObject: JSONObject) {

        //val routes = ArrayList<List<HashMap<String, String>>>()
        val jRoutes: JSONArray
        var jLegs: JSONArray
        var jSteps: JSONArray
        var jDuration: JSONObject
        var jDistance: JSONObject
        var maneuver = ""
        var instructions = ""
        var distance = ""
        var duration = ""

        //Coordinator of each step
        var start_location_lat = ""
        var start_location_lng = ""
        var count = 0

        //Transit mode
        //Bus/...
        var travelmode = ""
        var arrival_stop = "" //get name & location
        var arrival_time = "" //get text (text is time)
        var departure_stop = "" //get name & location
        var departure_time = "" //get text (text is time)
        var line_busname = "" //get line->name (example "name": 19 - Bến Thành - KCX Linh Trung - ĐH Quốc Gia)
        var headsign = "" //get headsign (name of arrival city)
        var num_stops = "" //get num_stops (number of stops)

        //walking in transit
        var jSteps_child: JSONArray
        var jDuration_child: JSONObject
        var jDistance_child: JSONObject
        var maneuver_child = ""
        var instructions_child = ""
        var distance_child = ""
        var duration_child = ""


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

                    stepsList.clear()
                    count = 0
                    for (k in 0 until jSteps.length()) {

                        instructions = ((jSteps.get(k) as JSONObject).get("html_instructions")) as String

                        Log.e("Step-0 instructions: ",  instructions)

                        distance = ((jSteps.get(k) as JSONObject).get("distance") as JSONObject).get("text") as String
                        Log.e("Step distance: ", distance)

                        duration = ((jSteps.get(k) as JSONObject).get("duration") as JSONObject).get("text") as String
                        Log.e("Step duration: ", duration)

                        start_location_lat = (((jSteps.get(k) as JSONObject).get("start_location") as JSONObject).get("lat") as Double).toString()
                        Log.e("Step duration: ", start_location_lat)

                        start_location_lng = (((jSteps.get(k) as JSONObject).get("start_location") as JSONObject).get("lng") as Double).toString()
                        Log.e("Step duration: ", start_location_lng)

                        Log.e("Step duration2: ", LatLng(start_location_lat.toDouble(),start_location_lng.toDouble()).toString())

                        travelmode = ((jSteps.get(k) as JSONObject).get("travel_mode")) as String

                        if (travelmode.toUpperCase() == "TRANSIT"){
                            maneuver = "transit"

                        } else if(travelmode.toUpperCase() == "WALKING"){
                            maneuver = "walking"

                            stepsList.add(DirectionsStepDbO(count++,maneuver,instructions,"",duration,distance,
                                    LatLng(start_location_lat.toDouble(),start_location_lng.toDouble()),
                                    TransitDbO("","","","","","","")))
                            getActivity()!!.runOnUiThread { adapterStepbyStepDirections.notifyDataSetChanged() }
                        }



                        if (travelmode == "TRANSIT"){
                            departure_stop = (((jSteps.get(k) as JSONObject).get("transit_details") as JSONObject)
                                    .get("departure_stop") as JSONObject).get("name") as String
                            departure_time = (((jSteps.get(k) as JSONObject).get("transit_details") as JSONObject)
                                    .get("departure_time") as JSONObject).get("text") as String
                            arrival_stop = (((jSteps.get(k) as JSONObject).get("transit_details") as JSONObject)
                                    .get("arrival_stop") as JSONObject).get("name") as String
                            arrival_time = (((jSteps.get(k) as JSONObject).get("transit_details") as JSONObject)
                                    .get("arrival_time") as JSONObject).get("text") as String

                            line_busname = (((jSteps.get(k) as JSONObject).get("transit_details") as JSONObject)
                                    .get("line") as JSONObject).get("name") as String
                            headsign = ((jSteps.get(k) as JSONObject).get("transit_details") as JSONObject)
                                    .get("headsign") as String
                            num_stops = (((jSteps.get(k) as JSONObject).get("transit_details") as JSONObject).get("num_stops") as Int).toString()

                            stepsList.add(DirectionsStepDbO(count++,maneuver,instructions,"",duration,distance,
                                    LatLng(start_location_lat.toDouble(),start_location_lng.toDouble()),
                                    TransitDbO(departure_stop,departure_time,arrival_stop,arrival_time,line_busname,headsign,num_stops)))
                            getActivity()!!.runOnUiThread { adapterStepbyStepDirections.notifyDataSetChanged() }

                        } else if(travelmode == "WALKING"){
                            //jSteps_child = (((jSteps.get(k) as JSONObject).get("transit_details") as JSONObject)
                            //        .get("departure_stop") as JSONObject).get("name") as String
                            jSteps_child = (jSteps.get(k) as JSONObject).getJSONArray("steps")

                            for (m in 0 until jSteps_child.length()) {

                                maneuver_child = if(((jSteps_child.get(m) as JSONObject).has("maneuver")) ){
                                    ((jSteps_child.get(m) as JSONObject).get("maneuver")) as String
                                } else "Head"
                                Log.e("Step maneuver: ", maneuver_child)

                                instructions_child = ((jSteps_child.get(m) as JSONObject).get("html_instructions")) as String

                                Log.e("Step-0 instructions: ",  instructions_child)

                                //Divide instructions string to instruction9 & attention
                                var instructions9 = instructions_child
                                var attention = ""           // just additional info about that step
                                var flag = false
                                var j = 0
                                var end = instructions_child.length - 1

                                while (j <= end){
                                    //
                                    if(instructions_child[j] == '<' && instructions_child[j+1] == 'd'){
                                        attention = instructions_child.substring(j,end+1)
                                        instructions9 = instructions_child.substring(0,j)
                                        Log.e("Step1 instructions9: ",  instructions9)
                                        Log.e("Step1 attention: ",  attention)
                                        break
                                    }
                                    j++
                                }

                                Log.e("Step1 instructions9: ",  instructions9)
                                Log.e("Step1 attention: ",  attention)


                                var start = 0
                                var i = 0
                                while(i < instructions9.length){

                                    if (instructions9[i] == '<') { start = i }

                                    if(instructions9[i] == '&' && instructions9[i+1] == 'a' && instructions9[i+2] == 'm'
                                            && instructions9[i+3] == 'p' && instructions9[i+4] == ';')
                                    {
                                        val first = instructions9.substring(0,i+1)
                                        val last = instructions9.substring(i+5,instructions9.length)
                                        instructions9 = first + last
                                    }

                                    if (instructions9[i] == '>')
                                    {
                                        val first = instructions9.substring(0,start)
                                        val last = instructions9.substring(i+1)
                                        val newins = first + last

                                        instructions9 = newins

                                        i=0
                                        start = 0
                                    }
                                    i++
                                }


                                var start2 = 0
                                var i2 = 0
                                while(i2 < attention.length){
                                    if (attention[i2] == '&' || attention[i2] == '<') { start2 = i2 }
                                    if (attention[i2] == ';' || attention[i2] == '>')
                                    {
                                        var spot = ""

                                        if(attention[i2 - 1] == 'v') spot = ". "

                                        val first = attention.substring(0,start2)
                                        val last = attention.substring(i2+1)
                                        val newins = first + spot + last

                                        Log.e("Step last: ",  last.toString())

                                        attention = newins

                                        i2=0
                                        Log.e("Step ins: ",  i2.toString())

                                        start2 = 0
                                    }
                                    i2++
                                }

                                Log.e("Step instructions: ",  instructions9)

                                distance_child = ((jSteps_child.get(m) as JSONObject).get("distance") as JSONObject).get("text") as String
                                Log.e("Step distance: ", distance)

                                duration_child = ((jSteps_child.get(m) as JSONObject).get("duration") as JSONObject).get("text") as String
                                Log.e("Step duration: ", duration)

                                start_location_lat = (((jSteps_child.get(m) as JSONObject).get("start_location") as JSONObject).get("lat") as Double).toString()
                                Log.e("Step duration: ", start_location_lat)

                                start_location_lng = (((jSteps_child.get(m) as JSONObject).get("start_location") as JSONObject).get("lng") as Double).toString()
                                Log.e("Step duration: ", start_location_lng)

                                Log.e("Step duration2: ", LatLng(start_location_lat.toDouble(),start_location_lng.toDouble()).toString())

                                stepsList.add(DirectionsStepDbO(count++,maneuver_child,instructions9,attention,duration_child,distance_child,
                                        LatLng(start_location_lat.toDouble(),start_location_lng.toDouble()),
                                        TransitDbO("","","","","","","")))
                                getActivity()!!.runOnUiThread { adapterStepbyStepDirections.notifyDataSetChanged() }
                            }
                        }

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
                val place = PlaceAutocomplete.getPlace(context, data)
                Log.e(TAG, "Place ID:" + place.id)
                val placeDB = PlaceEntity()
                placeDB.name = place.address.toString()
                /*placeDB.latitude = place.latLng.latitude
                placeDB.longitude = place.latLng.longitude
                placeDB.id = place.id*/

                edt_orgin.setText(placeDB.name)
                currentlocation = place.latLng

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                val status = PlaceAutocomplete.getStatus(context, data)
                Log.e(TAG, ""+status)
            } else if (resultCode == Activity.RESULT_CANCELED) {

            }
        } else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE2) {
            if (resultCode == Activity.RESULT_OK) {
                val place = PlaceAutocomplete.getPlace(context, data)
                Log.e(TAG, "Place ID:" + place.id)
                val placeDB = PlaceEntity()
                placeDB.name = place.address.toString()
                /*placeDB.latitude = place.latLng.latitude
                placeDB.longitude = place.latLng.longitude
                placeDB.id = place.id*/

                edt_destination.setText(placeDB.name)
                destlocation = place.latLng

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                val status = PlaceAutocomplete.getStatus(context, data)
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
        @RequiresApi(Build.VERSION_CODES.M)
        override fun doInBackground(vararg jsonData: String): List<List<HashMap<String, String>>>  {

            val jObject: JSONObject?
            try {
                jObject = JSONObject(jsonData[0])

                Log.d("ParserTask", jsonData[0])
                val parser = DataParser()
                if(transKind == "transit")
                {
                    StepByStep_Transit(jObject)
                } else  StepByStep(jObject)
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
                    //But, adapterTransportation setted on DirectionsFragment, So this is solution
                    getActivity()!!.runOnUiThread { adapterTransportation.notifyDataSetChanged() }

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
                    //But, adapterTransportation setted on DirectionsFragment, So this is solution
                    getActivity()!!.runOnUiThread { adapterTransportation.notifyDataSetChanged() }
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
        //mUiSettings.isZoomControlsEnabled = true     //it can be zoom control
        mUiSettings.isCompassEnabled = true          //....compass (la ban)
        mUiSettings.isMyLocationButtonEnabled = true //Enables or disables the my-location layer.
        mUiSettings.isScrollGesturesEnabled = true   //....cử chỉ scroll
        mUiSettings.isZoomGesturesEnabled = true     //...zoom
        mUiSettings.isTiltGesturesEnabled = true     //...Tilt (nghiêng)
        mUiSettings.isRotateGesturesEnabled = true   //...Rotate
        mUiSettings.isMapToolbarEnabled = true       // It ain't working, CHECKKKKKK
    }

    @Synchronized private fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(context!!)
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
        if ((ContextCompat.checkSelfPermission(context!!,
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
        if ((ContextCompat.checkSelfPermission(context!!,
                Manifest.permission.ACCESS_FINE_LOCATION) !== PackageManager.PERMISSION_GRANTED))
        {
            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this.activity!!,
                    Manifest.permission.ACCESS_FINE_LOCATION))
            {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this.activity!!,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        MY_PERMISSIONS_REQUEST_LOCATION)
            }
            else
            {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this.activity!!,
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
                    if ((ContextCompat.checkSelfPermission(context!!,
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
                    Toast.makeText(this.context, "permission denied", Toast.LENGTH_LONG).show()
                }
                return
            }
        }// other 'case' lines to check for other permissions this app might request.
        // You can add here other case statements according to your requirement.
    }
    companion object {
        val MY_PERMISSIONS_REQUEST_LOCATION = 99
        fun newInstance(): DirectionsFragment = DirectionsFragment()
    }
}

class LinearLayoutManagerWithSmoothScroller:LinearLayoutManager {
    constructor(context: Context) : super(context, VERTICAL, false) {}
    constructor(context:Context, orientation:Int, reverseLayout:Boolean) : super(context, orientation, reverseLayout) {}
    override fun smoothScrollToPosition(recyclerView:RecyclerView, state:RecyclerView.State,
                                        position:Int) {
        val smoothScroller = TopSnappedSmoothScroller(recyclerView.getContext())
        smoothScroller.setTargetPosition(position)
        startSmoothScroll(smoothScroller)
    }
    private inner class TopSnappedSmoothScroller(context:Context): LinearSmoothScroller(context) {
        //protected val verticalSnapPreference:Int
        override fun getVerticalSnapPreference(): Int {
            return SNAP_TO_START
        }

        override fun computeScrollVectorForPosition(targetPosition:Int): PointF {
            return this@LinearLayoutManagerWithSmoothScroller
                    .computeScrollVectorForPosition(targetPosition)
        }
    }
}

class SmoothLinearLayoutManager : LinearLayoutManager {

    private val millisecondsPreInch = 45f //default is 25f (bigger = slower)

    constructor(context: Context) : super(context, LinearLayoutManager.VERTICAL, false)

    constructor(context: Context, orientation: Int, reverseLayout: Boolean) : super(context, orientation, reverseLayout)

    override fun smoothScrollToPosition(recyclerView: RecyclerView, state: RecyclerView.State?,
                                        position: Int) {
        val smoothScroller = SmoothScroller(recyclerView.context)
        smoothScroller.targetPosition = position
        startSmoothScroll(smoothScroller)
    }

    private inner class SmoothScroller(context: Context) : LinearSmoothScroller(context) {

        override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
            return this@SmoothLinearLayoutManager.computeScrollVectorForPosition(targetPosition)
        }

        override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
            displayMetrics?.densityDpi?.let {
                return millisecondsPreInch / it
            }
            return super.calculateSpeedPerPixel(displayMetrics)
        }
    }
}
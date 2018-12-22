package com.horus.travelweather.activity

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.MenuItem
import com.horus.travelweather.R
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.horus.travelweather.database.PlaceEntity
import com.horus.travelweather.database.TravelWeatherDB
import com.horus.travelweather.adapter.LocationAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_add_location.*
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class AddLocationActivity : AppCompatActivity() {

    private val TAG = AddLocationActivity::class.java.simpleName
    var PLACE_AUTOCOMPLETE_REQUEST_CODE = 1
    private  val compositeDisposable = CompositeDisposable()
    private lateinit var adapter : LocationAdapter
    lateinit var database: FirebaseDatabase
    lateinit var place_list: DatabaseReference
    lateinit var mAuth: FirebaseAuth



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_location)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        loadPlaces()
        database = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()
        place_list = database.getReference("places").child(mAuth.currentUser!!.uid)
        btn_add_location.setOnClickListener {
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
    }

    //Load user's available places in database room -> recycleviewer rv_location
    private fun loadPlaces()
    {
        val  getAllPlace = TravelWeatherDB.getInstance(this).placeDataDao()
        compositeDisposable.add(getAllPlace.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    implementLoad(it)
                }, {
                        Log.e(TAG,""+ it.message)
                }))
    }

    // As LocationAdapter, input: list & id (by one click), if we click btn_delete_location on recycleviewer
    // (it was set a onclicklistener) of any place, it removed by call implementLoad().
    private fun implementLoad(list : List<PlaceEntity>) {
        adapter = LocationAdapter(list,{
            id ->
            deletePLace().execute(id)
            place_list.child(id).removeValue()
            adapter.notifyDataSetChanged()
        })
        val layoutManager : RecyclerView.LayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_location.adapter = adapter
        rv_location.layoutManager = layoutManager

    }

    //Use an intent to launch the autocomplete activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        //autocompleteFragment.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val place = PlaceAutocomplete.getPlace(this, data)
                Log.e(TAG, "Place ID:" + place.id)
                val placeDB = PlaceEntity()
                placeDB.name = place.name.toString()
                placeDB.latitude = place.latLng.latitude
                placeDB.longitude = place.latLng.longitude
                placeDB.id = place.id
                insertPLace().execute(placeDB)
                place_list.child(place.id).setValue(placeDB)
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                val status = PlaceAutocomplete.getStatus(this, data)
                Log.e(TAG, ""+status)
            } else if (resultCode == Activity.RESULT_CANCELED) {

            }
        }
    }

    //Press home button
    //Call this when your activity is done and should be closed.  The
    //* ActivityResult is propagated back to whoever launched you via (thông qua)
    //* onActivityResult().
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        if(id == android.R.id.home) {
            val intent = Intent()
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    inner class insertPLace(): AsyncTask<PlaceEntity, Void, Void>() {
        override fun doInBackground(vararg params: PlaceEntity): Void? {
                TravelWeatherDB.getInstance(this@AddLocationActivity).placeDataDao().insert(params[0])
            return null
        }
    }
    inner class deletePLace(): AsyncTask<String, Void, Void>() {
        override fun doInBackground(vararg params: String?): Void? {
            TravelWeatherDB.getInstance(this@AddLocationActivity).placeDataDao().deleteByPlaceId(params[0])
           return null
        }
    }
}
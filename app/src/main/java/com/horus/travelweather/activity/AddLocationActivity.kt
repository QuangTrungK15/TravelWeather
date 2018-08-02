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
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.horus.travelweather.database.PlaceData
import com.horus.travelweather.database.PlaceDatabase
import com.horus.travelweather.adapter.LocationAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_add_location.*


class AddLocationActivity : AppCompatActivity() {

    private val TAG = AddLocationActivity::class.java.simpleName

    var PLACE_AUTOCOMPLETE_REQUEST_CODE = 1

    private  val compositeDisposable = CompositeDisposable()

    private lateinit var adapter :LocationAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_location)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        loadPlaces()
        btn_add_location.setOnClickListener {
            val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                    .build(this)
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
        }
    }


    private fun loadPlaces()
    {
        val  getAllPlace = PlaceDatabase.getInstance(this).placeDataDao()
        compositeDisposable.add(getAllPlace.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    implementLoad(it)
                }, {
                        Log.e(TAG,""+ it.message)
                }))
    }

    private fun implementLoad(list : List<PlaceData>) {

        adapter = LocationAdapter(list,{
            id ->
            deletePLace().execute(id)
            adapter.notifyDataSetChanged()
        })
        val layoutManager : RecyclerView.LayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_location.adapter = adapter
        rv_location.layoutManager = layoutManager


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        //autocompleteFragment.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val place = PlaceAutocomplete.getPlace(this, data)
                Log.e(TAG, "Lat:" + place.latLng.latitude)
                Log.e(TAG, "Lon:" + place.latLng.longitude)
//                val placeDB = PlaceData(0,place.name.toString(),place.latLng.latitude,place.latLng.longitude)
                val placeDB = PlaceData()
                placeDB.name = place.name.toString()
                placeDB.latitude = place.latLng.latitude
                placeDB.longitude = place.latLng.longitude
                insertPLace().execute(placeDB)
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                val status = PlaceAutocomplete.getStatus(this, data)
                Log.e(TAG, status.statusMessage)
            } else if (resultCode == Activity.RESULT_CANCELED) {

            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id =   item?.itemId
        if(id == android.R.id.home) {
            var intent = Intent()
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    inner class insertPLace(): AsyncTask<PlaceData, Void, Void>() {
        override fun doInBackground(vararg params: PlaceData): Void? {
                PlaceDatabase.getInstance(this@AddLocationActivity).placeDataDao().insert(params[0])
            return null
        }
    }



    inner class deletePLace(): AsyncTask<Int, Void, Void>() {
        override fun doInBackground(vararg params: Int?): Void? {
            PlaceDatabase.getInstance(this@AddLocationActivity).placeDataDao().deleteByPlaceId(params[0])
           return null
        }
    }





    inner class deleteAllPLace(): AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            PlaceDatabase.getInstance(this@AddLocationActivity).placeDataDao().deleteAll()
            return null
        }

    }


}
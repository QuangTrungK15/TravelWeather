package com.horus.travelweather.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.location.Geocoder
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.horus.travelweather.BottomNavigation
import com.horus.travelweather.R
import com.horus.travelweather.adapter.HistoryAdapter
import com.horus.travelweather.adapter.LocationAdapter
import com.horus.travelweather.database.PlaceEntity
import com.horus.travelweather.database.TravelWeatherDB
import com.horus.travelweather.model.CitySatisticsDbO
import com.horus.travelweather.model.HistoryDbO
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_add_location.*
import java.io.IOException
import java.util.*


class AddLocationActivity : AppCompatActivity() {

    private val TAG = AddLocationActivity::class.java.simpleName
    var PLACE_AUTOCOMPLETE_REQUEST_CODE = 1
    private  val compositeDisposable = CompositeDisposable()
    private lateinit var adapter : LocationAdapter
    lateinit var database: FirebaseDatabase
    lateinit var place_list: DatabaseReference
    lateinit var mAuth: FirebaseAuth

    private val historyDb = HistoryDbO()
    lateinit var history_list: DatabaseReference
    lateinit var adapter2: FirebaseRecyclerAdapter<HistoryDbO, HistoryAdapter.HistoryViewHolder>
    var myuser: FirebaseUser? = null

    lateinit var city_statistics: DatabaseReference //for statistics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_location)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val actionBar1 = supportActionBar
        if (actionBar1 != null) {
            //actionBar1.setDisplayHomeAsUpEnabled(true)
            actionBar1.title = "Danh Sách Thời Tiết"
        }

        loadPlaces()
        database = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()
        place_list = database.getReference("places").child(mAuth.currentUser!!.uid)
        myuser = mAuth.currentUser
        history_list = database.getReference("history")
        city_statistics = database.getReference("city_statistics")


        btn_add_location.text = "Danh Sách Thời Tiết"
        btn_add_location.setOnClickListener {
            //Filter results by place type (by address: get full address, by establisment: get business address)
            val typeFilter = AutocompleteFilter.Builder()
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
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

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, BottomNavigation::class.java) //this activity will be this fragment's father
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    var numofplace = 0
    //Load user's available places in database room -> recycleviewer rv_location
    private fun loadPlaces()
    {
        val  getAllPlace = TravelWeatherDB.getInstance(this).placeDataDao()
        compositeDisposable.add(getAllPlace.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    implementLoad(it)
                    numofplace=it.size
                    Log.e(TAG,"sizeeee : "+ numofplace)

                }, {
                    Log.e(TAG,""+ it.message)
                }))
    }

    // As LocationAdapter, input: list & id (by one click), if we click btn_delete_location on recycleviewer
    // (it was set a onclicklistener) of any place, it removed by call implementLoad().
    private fun implementLoad(list : List<PlaceEntity>) {
        adapter = LocationAdapter(list,{
            id ->

                val alertDialogBuilder = AlertDialog.Builder(this)
                alertDialogBuilder.setTitle("Xóa Địa Điểm Thời Tiết")
                alertDialogBuilder
                        .setMessage("Bạn có thật sự muốn xóa địa điểm thời tiết này?")
                        .setCancelable(false)
                        .setPositiveButton("Yes") { dialog, id2 ->
                            deletePLace().execute(id)
                            place_list.child(id).removeValue()
                            adapter.notifyDataSetChanged()
                        }
                        .setNegativeButton("No") { dialog, id2 ->
                            dialog.cancel()
                        }
                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()

        })
        if(adapter.itemCount != 0){
            btn_add_location.text = "Thêm địa điểm thời tiết"
        } else btn_add_location.text = "Danh Sách Thời Tiết"

        val layoutManager : RecyclerView.LayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_location.adapter = adapter
        rv_location.layoutManager = layoutManager

    }
    fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
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
                placeDB.latitude = place.latLng.latitude
                placeDB.longitude = place.latLng.longitude
                placeDB.name = getCityName_byLatlong(place.latLng)
                cityname_temp = placeDB.name

                //placeDB.name = place.locale.toString()
                placeDB.id = place.id
                if(numofplace < 4){
                    insertPLace().execute(placeDB)
                    place_list.child(place.id).setValue(placeDB)
                } else {
                    val alertDialogBuilder = AlertDialog.Builder(this)
                    alertDialogBuilder.setTitle("Thông Báo")
                    alertDialogBuilder
                            .setMessage("Bạn chỉ được thêm tối đa 4 địa điểm thời tiết hay ghé thăm để theo dõi."+
                            " Để thêm địa điểm mới, vui lòng xóa địa điểm bạn không còn quan tâm!")
                            .setCancelable(false)
                            .setPositiveButton("OK") { dialog, id ->
                                dialog.cancel()
                            }
                    val alertDialog = alertDialogBuilder.create()
                    alertDialog.show()
                }


                uploadCitySatistics()

                //history object
                historyDb.address = place.address.toString()
                historyDb.name = place.name.toString()
                //historyDb.placeTypes = place.placeTypes.toString()
                historyDb.historyId = place.id
                val date = getCurrentDateTime()
                //val c = GregorianCalendar(1995, 12, 23)
                val currenttime = String.format("%1\$td/%1\$tm/%1\$tY", date)
                historyDb.date = currenttime
                uploadDatabase() //add to firebase
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                val status = PlaceAutocomplete.getStatus(this, data)
                Log.e(TAG, ""+status)
            } else if (resultCode == Activity.RESULT_CANCELED) {

            }
        }
    }

    var cityname_temp = ""
    var citysatistics_flag = true
    private fun uploadCitySatistics() {

        city_statistics.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.e(TAG, "Error : " + p0.message)
            }

            var numofsearch_others = 0
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.exists()) {
                    // code if data exists
                    // if current place like before place
                    for (dsp in dataSnapshot.children) {
                        //add result into array list
                        val item: CitySatisticsDbO? = dsp.getValue(CitySatisticsDbO::class.java)
                        if (item != null) {
                            if(item.name == "Others") numofsearch_others = item.numofsearch
                            if ((cityname_temp == item.name || cityname_temp == "Thành phố " + item.name ||
                                            cityname_temp == "Thủ Đô " + item.name ||
                                            cityname_temp == "Tỉnh " + item.name)) {
                                Log.e("lamquanglich : ",dsp.key)
                                city_statistics.child(dsp.key!!).setValue(CitySatisticsDbO(item.name,item.numofsearch+1))
                                citysatistics_flag = false

                            }
                        }
                    }

                } else {
                    // code if data does not  exists
                    if(cityname_temp != ""){
                        if((cityname_temp.toLowerCase() == "hồ chí minh" || cityname_temp == "thành phố hồ chí minh") ||
                                (cityname_temp.toLowerCase() == "hà nội" || cityname_temp == "thủ đô hà nội") ||
                                (cityname_temp.toLowerCase() == "đà nẵng" || cityname_temp == "thành phố đà nẵng") ||
                                (cityname_temp.toLowerCase() == "cần thơ" || cityname_temp == "thành phố cần thơ")
                        ){
                            city_statistics.push().setValue(CitySatisticsDbO(cityname_temp,1))

                        } else {
                            city_statistics.child("-Li261TH2CuzJV9lyWvM").setValue(CitySatisticsDbO("Others",numofsearch_others+1))
                        }

                        citysatistics_flag = false
                    }
                }
                if (citysatistics_flag && cityname_temp != "") {
                    if((cityname_temp.toLowerCase() == "hồ chí minh" || cityname_temp == "thành phố hồ chí minh") ||
                            (cityname_temp.toLowerCase() == "hà nội" || cityname_temp == "thủ đô hà nội") ||
                            (cityname_temp.toLowerCase() == "đà nẵng" || cityname_temp == "thành phố đà nẵng") ||
                            (cityname_temp.toLowerCase() == "cần thơ" || cityname_temp == "thành phố cần thơ")
                    ){
                        city_statistics.push().setValue(CitySatisticsDbO(cityname_temp,1))

                    } else {
                        city_statistics.child("-Li261TH2CuzJV9lyWvM").setValue(CitySatisticsDbO("Others",numofsearch_others+1))
                    }
                }
            }
        })
    }

    fun getCityName_byLatlong(latlong: LatLng): String {
        //get city name
        val geocoder = Geocoder(this, Locale.getDefault())
        val latitude_temp = latlong.latitude
        val longitude_temp = latlong.longitude
        val cityname_temp2 = ""
        try
        {
            val addresses = geocoder.getFromLocation(latitude_temp, longitude_temp, 1)

            if (addresses != null)
            {
                Log.e("start location : ", addresses.toString())

                val returnedAddress = addresses.get(0)
                val strReturnedAddress = StringBuilder("Address:\n")
                //val strReturnedAddress = StringBuilder()

                for (i in 0 until returnedAddress.getMaxAddressLineIndex())
                {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
                }
                //Log.e("start location : ", addresses.get(0).subAdminArea)
                return addresses.get(0).adminArea
            }
            else
            {
                Log.d("a","No Address returned! : ")
            }
        }
        catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
            Log.d("a","Canont get Address!")
        }
        //end get city name
        return cityname_temp2
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

    private fun uploadDatabase() {
        history_list.child(myuser!!.uid).push().setValue(historyDb)
    }
}
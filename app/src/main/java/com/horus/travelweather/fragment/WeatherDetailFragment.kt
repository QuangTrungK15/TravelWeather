package com.horus.travelweather.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.horus.travelweather.R
import com.horus.travelweather.common.TWConstant
import com.horus.travelweather.repository.Repository
import com.horus.travelweather.service.ApiService
import com.horus.travelweather.utils.StringFormatter.convertTimestampToDayAndHourFormat
import com.horus.travelweather.utils.StringFormatter.convertToValueWithUnit
import com.horus.travelweather.utils.StringFormatter.unitDegreesCelsius
import com.horus.travelweather.utils.StringFormatter.unitPercentage
import com.horus.travelweather.utils.StringFormatter.unitsMetresPerSecond
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_weather_details.*
import android.location.Geocoder
import android.location.Location
import java.util.*
import android.location.LocationManager
import android.opengl.Visibility
import android.os.AsyncTask
import android.os.Build
import android.preference.PreferenceManager
import android.support.annotation.RequiresApi
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.format.DateFormat
import android.widget.Toast
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.places.PlaceBuffer
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.horus.travelweather.BottomNavigation
import com.horus.travelweather.R.id.*
import com.horus.travelweather.activity.DataParser
import com.horus.travelweather.adapter.DailyWeatherAdapter
import com.horus.travelweather.database.PlaceEntity
import com.horus.travelweather.model.*
import com.horus.travelweather.utils.StringFormatter.getCurrentTime
import com.patloew.rxlocation.RxLocation
import ru.solodovnikov.rx2locationmanager.LocationTime
import ru.solodovnikov.rx2locationmanager.RxLocationManager
import java.util.concurrent.TimeUnit
import ru.solodovnikov.rx2locationmanager.LocationRequestBuilder
import io.reactivex.internal.operators.single.SingleInternalHelper.toObservable
import kotlinx.android.synthetic.main.activity_bottom_navigation.*
import kotlinx.android.synthetic.main.activity_directions.view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class WeatherDetailFragment : Fragment() {

    private val TAG = WeatherDetailFragment::class.java.simpleName
    lateinit var database: FirebaseDatabase
    lateinit var place_list: DatabaseReference
    lateinit var tempplace_list: DatabaseReference
    lateinit var mAuth: FirebaseAuth
    private val tempplaceDb = TempPlaceDbO()

    @SuppressLint("CheckResult")
    private fun requestWeatherDetails(lat: Double, long: Double) {
        Repository.createService(ApiService::class.java).getWeatherDetailsCoordinates(lat, long, TWConstant.ACCESS_API_KEY)
                .observeOn(AndroidSchedulers.mainThread()) // Chi dinh du lieu chinh tren mainthread
                .subscribeOn(Schedulers.io())//chi dinh cho request lam viec tren I/O Thread(request to api ,  download a file,...)
                .subscribe(
                        //cú pháp của rxjava trong kotlin
                        { result ->
                            cityname_temp = getCityName_byLatlong (LatLng(lat,long))
                            //request thành công
                            processResponseData(result)
                        },
                        { error ->
                            //request thất bai
                            handlerErrorWeatherDetails(error)
                        }
                )

        Repository.createService(ApiService::class.java).getDailyWeatherCoordinates(lat, long, TWConstant.ACCESS_API_KEY)
                .observeOn(AndroidSchedulers.mainThread()) // Chi dinh du lieu chinh tren mainthread
                .subscribeOn(Schedulers.io())//chi dinh cho request lam viec tren I/O Thread(request to api ,  download a file,...)
                .subscribe(
                        //cú pháp của rxjava trong kotlin
                        { result ->
                            cityname_temp = getCityName_byLatlong (LatLng(lat,long))
                            //request thành công
                            processResponseDataDaily(result)
                        },
                        { error ->
                            //request thất bai
                            handlerErrorWeatherDetails(error)
                        }
                )
    }

    private fun handlerErrorWeatherDetails(error: Throwable?) {
        Log.e(TAG, "" + error.toString())
    }

    private fun processResponseDataDaily(result: DailyWeatherDetailResponse?) {
        //Log.e(TAG, "111111111" + result!!.list[0].temperature.temp)
        val adapter = DailyWeatherAdapter(result!!.list)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        rv_hourly_weather_list.layoutManager = layoutManager
        rv_hourly_weather_list.adapter = adapter
    }

    private fun processResponseData(result: WeatherDetailsResponse) {
        txt_current_time.text = getCurrentTime()
        txt_date_time.text = convertTimestampToDayAndHourFormat(result.dateTime)
        if(cityname_temp != "") txt_city_name.text = cityname_temp
        else txt_city_name.text = result.nameCity
        //cityname_temp = result.nameCity //using to add to tempplace

        txt_temperature.text = convertToValueWithUnit(0, unitDegreesCelsius, convertKelvinToCelsius(result.temperature.temp))
        Log.e("nhiet do",txt_temperature.text.toString())
        txt_temp_max.text = convertToValueWithUnit(0, unitDegreesCelsius, convertKelvinToCelsius(result.temperature.temp_max))
        txt_temp_min.text = convertToValueWithUnit(0, unitDegreesCelsius, convertKelvinToCelsius(result.temperature.temp_min))
        txt_main_weather.text = result.weather[0].nameWeather

        txt_humidity.text = convertToValueWithUnit(0, unitPercentage, result.temperature.humidity)
        txt_wind.text = convertToValueWithUnit(0, unitsMetresPerSecond, result.wind.speed)
        txt_cloud_cover.text = result.clouds.all.toString()
        Picasso.with(this.context).load(TWConstant.BASE_URL_UPLOAD + result.weather[0].icon + ".png").into(img_weather_icon)
    }

    private fun convertKelvinToCelsius(temperatue: Double): Double {
        val temp = (temperatue - 273.15)
        return temp
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        requestLocation()
        val view = inflater.inflate(R.layout.fragment_weather_details, container, false)
        return view
    }

    private fun addTempplace(){

    }

    var cityname_temp = ""
    var placeid_temp = ""

    var latitude_temp = 0.0
    var longitude_temp = 0.0
    var numofvisit_temp = 0
    var isacity_temp = false
    var newplace_flag = true
    var runonce_flag = true
    var curplace_like_beforeplace = false

    //Now Location
    @SuppressLint("CheckResult")
    private fun requestLocation() {
        //database = FirebaseDatabase.getInstance()
        //mAuth = FirebaseAuth.getInstance()
        //tempplace_list = database.getReference("tempplace").child(mAuth.currentUser!!.uid)
        //place_list = database.getReference("places").child(mAuth.currentUser!!.uid)



        val rxLocationManager = context?.let { RxLocationManager(it) }
        if(runonce_flag == true){

        if(arguments!!.getInt("position")==0) {

            if (rxLocationManager != null) {
                rxLocationManager.requestLocation(LocationManager.NETWORK_PROVIDER)
                        .subscribe({
                            val geocode = Geocoder(context, Locale.getDefault())
                            val address = geocode.getFromLocation(it.latitude, it.longitude, 1)
                            requestWeatherDetails(address[0].latitude, address[0].longitude)
                            latitude_temp = it.latitude
                            longitude_temp = it.longitude
                            //get city name
                            val geocoder = Geocoder(context!!, Locale.getDefault())

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
                                    Log.e("start location : ", addresses.get(0).subAdminArea)
                                    cityname_temp = addresses.get(0).adminArea
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
                            //end get city name

                            //get placeid
                            val apikey="AIzaSyDc6fdew54ONuhKNVCCV6urWWL-1WWMmBI"
                            val url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=$latitude_temp,$longitude_temp&language=vi&key=$apikey"

                            val fetchUrl = FetchUrl()
                            fetchUrl.execute(url)


                        }, {
                            Log.e(TAG, "Error" + it.message)
                        })
            }
        }
        else {
            val geocode = Geocoder(activity, Locale.getDefault())
            val address = geocode.getFromLocation(arguments!!.getDouble("lat"), arguments!!.getDouble("lon"), 1)
            requestWeatherDetails(address.get(0).latitude, address.get(0).longitude)
        }
            runonce_flag = false
        }
    }

    private inner class FetchUrl() : AsyncTask<String, Void, String>() {
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


        override fun onPostExecute(result:String) {
            Log.d("onPostExecue resute", result)
            super.onPostExecute(result)
            val parserTask = ParserTask()
            // Invokes the thread for parsing the JSON data
            parserTask.execute(result)
        }
    }

    fun getPlaceId(jObject: JSONObject):String {

        val jResults: JSONArray
        var jPlaceId = ""
        try {

            jResults = jObject.getJSONArray("results")

            /** Traversing all routes  */
            //for (i in 0 until jResults.length()) {
                jPlaceId = ((jResults.get(0) as JSONObject).get("place_id")) as String
                placeid_temp = jPlaceId
                Log.e("Step duration: ",placeid_temp)
            return placeid_temp

            //}
        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: Exception) {
        }
        return jPlaceId
    }

    private inner class ParserTask() : AsyncTask<String, Int, List<List<HashMap<String, String>>>>() {


        // Parsing the data in non-ui thread
        @RequiresApi(Build.VERSION_CODES.M)
        override fun doInBackground(vararg jsonData: String): List<List<HashMap<String, String>>>  {

            val jObject: JSONObject?
            try {
                jObject = JSONObject(jsonData[0])

                Log.d("ParserTask", jsonData[0])
                val parser = DataParser()
                placeid_temp = getPlaceId(jObject)
                Log.e("Step duration: ",placeid_temp)

                Log.d("ParserTask", parser.toString())

                // Starts parsing data
                val routes: List<List<HashMap<String, String>>>  = parser.parse(jObject)

                //parse2: get duration of this all route
                //val duration:  String  = parser.parse2(jObject)!!

                //Log.d("ParaserTask", "Executing routes")
                //Log.d("ParserTask", routes.toString())

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
                /*var points: ArrayList<LatLng>
                // Traversing through all the routes
                for (i in result.indices) {
                    points = ArrayList<LatLng>()

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

                    Log.d("onPostExecute", "onPostExecute lineoptions decoded")

                }*/
            database = FirebaseDatabase.getInstance()
            mAuth = FirebaseAuth.getInstance()
            tempplace_list = database.getReference("tempplace").child(mAuth.currentUser!!.uid)

            place_list = database.getReference("places").child(mAuth.currentUser!!.uid)
            //val placeList = ArrayList<PlaceEntity>()
            val cityname_list = ArrayList<String>()
            var index_temp = 0

            place_list.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Result will be holded Here
                    for (dsp in dataSnapshot.children) {
                        //add result into array list
                        val item : PlaceEntity? = dsp.getValue(PlaceEntity::class.java)
                        if (item != null) {
                            //placeList.add(item)
                            cityname_list.add(getCityName_byLatlong(LatLng(item.latitude,item.longitude)))
                            //cityname_list[index_temp++]=)
                            Log.d("city name : ",cityname_list.get(index_temp))
                           // placeList.add(PlaceEntity(item.id,cityname_list.get(index_temp),item.latitude,item.longitude))
                            index_temp++
                        }
                    }
                }


            })
                tempplace_list.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        Log.e(TAG, "Error : " + p0.message)
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        if (dataSnapshot.exists()) {
                            // code if data exists
                            for (dsp in dataSnapshot.children) {
                                //add result into array list
                                val item: TempPlaceDbO? = dsp.getValue(TempPlaceDbO::class.java)
                                if (item != null) {
                                    //Log.e("Test AI : ",dsp.key)
                                    var now_cityname = ""

                                    if (dsp.key == mAuth.currentUser!!.uid && item.name == cityname_temp) {
                                        curplace_like_beforeplace = true
                                        now_cityname = item.name
                                        //Log.e("Test AI : ",placeid_temp + item.name + cityname_temp)
                                        //break
                                    }

                                    //Add new place if temp place (visit:1 or searchh: 5)
                                    val date = getCurrentDateTime()
                                    val currenttime = String.format("%1\$td/%1\$tm/%1\$tY", date)

                                    if(cityname_list.contains(item.name) == true){

                                    } else if(dsp.key != mAuth.currentUser!!.uid &&
                                            (item.numofvisit >= 1 || item.numofsearch > 4) && item.numofask < 3
                                    && currenttime != item.askdate){
                                        val alertDialogBuilder = AlertDialog.Builder(context)
                                        alertDialogBuilder.setTitle("Thêm địa điểm thời tiết")
                                        alertDialogBuilder
                                                .setMessage("Bạn có muốn thêm "+item.name+" vào màn hình để tiện quan sát" +
                                                        " thời tiết hay không?")
                                                .setCancelable(false)
                                                .setPositiveButton("Yes") { dialog, id ->
                                                    // Add new place if temp place qualified
                                                    //for(pl in placeList){
                                                    //    if(pl.name == item.name){
                                                            val placeDB = PlaceEntity()
                                                            placeDB.name = item.name.toString()
                                                            placeDB.latitude = item.latitude
                                                            placeDB.longitude = item.longitude
                                                            placeDB.id = item.id
                                                            place_list.child(item.id).setValue(placeDB)

                                                      //  }
                                                    //}
                                                    Toast.makeText(context, "You added successfully.", Toast.LENGTH_SHORT).show()
                                                    val intent = Intent(context, BottomNavigation::class.java) //this activity will be this fragment's father
                                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                    startActivity(intent)
                                                }
                                                .setNegativeButton("No") { dialog, id ->
                                                    // if this button is clicked, just close
                                                    // the dialog box and do nothing
                                                    tempplaceDb.latitude = item.latitude
                                                    tempplaceDb.longitude = item.longitude
                                                    tempplaceDb.name = item.name
                                                    tempplaceDb.placename = item.placename
                                                    tempplaceDb.numofvisit = item.numofvisit
                                                    tempplaceDb.numofsearch = item.numofsearch
                                                    tempplaceDb.isacity = item.isacity
                                                    tempplaceDb.id = item.id
                                                    tempplaceDb.numofask = item.numofask+1
                                                    tempplaceDb.askdate = currenttime
                                                    tempplace_list.child(item.id).setValue(tempplaceDb)

                                                    if(curplace_like_beforeplace){
                                                        tempplace_list.child(mAuth.currentUser!!.uid).setValue(tempplaceDb)
                                                        curplace_like_beforeplace = false
                                                    }

                                                    dialog.cancel()
                                                }
                                        val alertDialog = alertDialogBuilder.create()
                                        alertDialog.show()
                                    }


                                }
                            }
                            if(!curplace_like_beforeplace) {

                                for (dsp in dataSnapshot.children) {
                                    //add result into array list
                                    val item: TempPlaceDbO? = dsp.getValue(TempPlaceDbO::class.java)
                                    if (item != null) {

                                        if ((cityname_temp == item.name || cityname_temp == "Thành phố " + item.name ||
                                                cityname_temp == "Thủ Đô " + item.name ||
                                                cityname_temp == "Tỉnh " + item.name) &&
                                                item.isacity == true && curplace_like_beforeplace == false) {


                                            //place_list.child(place.id).setValue(placeDB)
                                            //tempplaceDb.numofvisit = item.numofvisit+1

                                            tempplaceDb.latitude = item.latitude
                                            tempplaceDb.longitude = item.longitude
                                            tempplaceDb.name = item.name
                                            tempplaceDb.placename = item.placename
                                            tempplaceDb.numofvisit = item.numofvisit + 1
                                            tempplaceDb.numofsearch = item.numofsearch
                                            tempplaceDb.isacity = item.isacity
                                            tempplaceDb.id = item.id
                                            tempplaceDb.numofask = item.numofask
                                            //val date = getCurrentDateTime()
                                            //val currenttime = String.format("%1\$td/%1\$tm/%1\$tY", date)
                                            tempplaceDb.askdate = item.askdate
                                            tempplace_list.child(item.id).setValue(tempplaceDb)

                                            //update dia diem hien tai gan nhat da ghe qua
                                            tempplace_list.child(mAuth.currentUser!!.uid).setValue(tempplaceDb)


                                            newplace_flag = false
                                        }
                                    }
                                }
                            }
                        } else {
                            // code if data does not  exists
                            if(!curplace_like_beforeplace) {

                                tempplaceDb.latitude = latitude_temp
                                tempplaceDb.longitude = longitude_temp
                                tempplaceDb.name = cityname_temp
                                tempplaceDb.numofvisit = 1
                                tempplaceDb.isacity = true
                                tempplaceDb.id = placeid_temp
                                val date = getCurrentDateTime()
                                val currenttime = String.format("%1\$td/%1\$tm/%1\$tY", date)
                                tempplaceDb.askdate = currenttime
                                //update dia diem hien tai gan nhat da ghe qua
                                tempplace_list.child(mAuth.currentUser!!.uid).setValue(tempplaceDb)

                                newplace_flag = false
                            }
                        }
                        if (newplace_flag && !curplace_like_beforeplace) {
                            tempplaceDb.latitude = latitude_temp
                            tempplaceDb.longitude = longitude_temp
                            tempplaceDb.name = cityname_temp
                            tempplaceDb.numofvisit = 1
                            tempplaceDb.isacity = true
                            tempplaceDb.id = placeid_temp
                            val date = getCurrentDateTime()
                            val currenttime = String.format("%1\$td/%1\$tm/%1\$tY", date)
                            tempplaceDb.askdate = currenttime
                            tempplace_list.child(tempplaceDb.id).setValue(tempplaceDb)

                            //update dia diem hien tai gan nhat da ghe qua
                            tempplace_list.child(mAuth.currentUser!!.uid).setValue(tempplaceDb)
                        }
                        // Result will be holded Here

                        //insertAllPlace().execute(placeList)
                    }
                })
        }
    }
    fun getCityName_byLatlong(latlong: LatLng): String {
        //get city name
        val geocoder = Geocoder(context!!, Locale.getDefault())
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
                var result_adminarea = addresses.get(0).adminArea
                if(result_adminarea == null) result_adminarea = ""
                return result_adminarea
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
        //end get city name
        return cityname_temp2
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

    fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }

}
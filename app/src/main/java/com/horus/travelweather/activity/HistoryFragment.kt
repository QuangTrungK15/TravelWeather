package com.horus.travelweather.activity

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewCompat.setNestedScrollingEnabled
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.TextView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.horus.travelweather.R
import com.horus.travelweather.R.id.navigation_direction
import com.horus.travelweather.adapter.HistoryAdapter
import com.horus.travelweather.common.TWConstant
import com.horus.travelweather.database.PlaceEntity
import com.horus.travelweather.model.HistoryDbO
import kotlinx.android.synthetic.main.eachhistory_layout.view.*
import kotlinx.android.synthetic.main.fragment_history.view.*
import java.util.*

/**
 * Created by onlyo on 12/26/2018.
 */
class HistoryFragment: Fragment() {

    private val TAG = HistoryFragment::class.java.simpleName
    private val historyDb = HistoryDbO()
    lateinit var database: FirebaseDatabase
    lateinit var history_list: DatabaseReference
    lateinit var history_list_temp: DatabaseReference
    lateinit var mAuth: FirebaseAuth
    lateinit var adapter: FirebaseRecyclerAdapter<HistoryDbO, HistoryAdapter.HistoryViewHolder>
    var myuser: FirebaseUser? = null
    var length: Int = 0
    var runagain: Int = 0
    lateinit var placeList: ArrayList<HistoryDbO>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        setHasOptionsMenu(true)
        mAuth = FirebaseAuth.getInstance()
        myuser = mAuth.currentUser
        database = FirebaseDatabase.getInstance()
        history_list = database.getReference("history")
        history_list_temp = database.getReference("history").child(mAuth.currentUser!!.uid)
        //length = 0
        placeList = ArrayList<HistoryDbO>()
        val options = FirebaseRecyclerOptions.Builder<HistoryDbO>()
                .setQuery(history_list_temp, HistoryDbO::class.java)
                .setLifecycleOwner(this)
                .build()

        history_list_temp.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.e(TAG,"Error : "+p0.message)
            }
            override fun onDataChange(dataSnapshot : DataSnapshot) {

                // Result will be holded Here
                for (dsp in dataSnapshot.children) {
                    //add result into array list
                    val item : HistoryDbO? = dsp.getValue(HistoryDbO::class.java)
                    if (item != null) {
                        placeList.add(item)
                        length++
                    }
                }
                //Log.e(TAG,"Size : "+placeList.size)
                length = placeList.size

                //Collections.reverse(placeList)
                for (i in placeList) {
                    //Log.e(TAG,"Size2 : "+i.name)
                }


                //insertAllPlace().execute(placeList)
            }
        })

        adapter = HistoryAdapter(options, placeList, runagain, { context,textview, i ->
            showPopup(context,textview, i)
        })

        val linearLayoutManager = LinearLayoutManager(this.activity)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        view.rv_history.layoutManager = linearLayoutManager as RecyclerView.LayoutManager?
        view.rv_history.adapter = adapter
        view.rv_history.isNestedScrollingEnabled = false
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        if (inflater != null) {
            inflater.inflate(R.menu.option_menu, menu)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.removeall -> {
                deleteAllHistory()
                return true
            }
        }
        return false
    }

    companion object {
        fun newInstance(): HistoryFragment = HistoryFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    //Click popup menu of any img
    private fun showPopup(context: Context, textView: TextView, position: Int) {
        var popup: PopupMenu? = null
        popup = PopupMenu(context, textView)
        //Add only option (remove) of per img
        popup.menu.add(0, position, 0, TWConstant.REMOVE_PLACE)
        popup.show()
        popup.setOnMenuItemClickListener({
            Log.e(TAG,"key : "+(length - 1 - position))
            deleteOneHistory(adapter.getRef(position).key!!,position) //get position id of rv_my_places
            runagain++
            true
        })

        /*if (inflater != null) {
            inflater.inflate(R.menu.option_menu, menu)

            val item1 = menu!!.findItem(R.id.show_historyinfo)
            val item2 = menu!!.findItem(R.id.removeall)

            item1.actionView.setOnTouchListener{ view: View, motionEvent: MotionEvent ->
                when (motionEvent.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_HOVER_ENTER -> {

                        item2.setOnMenuItemClickListener({
                            deleteAllHistory() //get position id of rv_my_places
                            true
                        })
                    }
                }
                true
            }
        }*/
    }

    private fun deleteAllHistory() {
        history_list_temp.removeValue()
        adapter.notifyDataSetChanged()
    }
    private fun deleteOneHistory(key: String, tempkey: Int) {
        history_list_temp.child(key).removeValue()
        runagain++
        placeList.removeAt(tempkey)
        //adapter.notifyItemRemoved(tempkey)
        adapter.notifyDataSetChanged()
    }
    private fun uploadDatabase() {
        history_list_temp.push().setValue(historyDb)
    }

}
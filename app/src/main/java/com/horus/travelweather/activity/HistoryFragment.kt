package com.horus.travelweather.activity

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.TextView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.horus.travelweather.R
import com.horus.travelweather.adapter.HistoryAdapter
import com.horus.travelweather.common.TWConstant
import com.horus.travelweather.model.HistoryDbO
import kotlinx.android.synthetic.main.fragment_history.view.*

/**
 * Created by onlyo on 12/26/2018.
 */
class HistoryFragment: Fragment() {

    private val TAG = HistoryFragment::class.java.simpleName
    private val historyDb = HistoryDbO()
    lateinit var database: FirebaseDatabase
    lateinit var history_list: DatabaseReference
    lateinit var mAuth: FirebaseAuth
    lateinit var adapter: FirebaseRecyclerAdapter<HistoryDbO, HistoryAdapter.HistoryViewHolder>
    var myuser: FirebaseUser? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        setHasOptionsMenu(true)
        mAuth = FirebaseAuth.getInstance()
        myuser = mAuth.currentUser
        database = FirebaseDatabase.getInstance()
        history_list = database.getReference("history")


        val options = FirebaseRecyclerOptions.Builder<HistoryDbO>()
                .setQuery(history_list.child(myuser!!.uid), HistoryDbO::class.java)
                .setLifecycleOwner(this)
                .build()
        adapter = HistoryAdapter(options, { context,textview, i ->
            showPopup(context,textview, i)
        })
        view.rv_history.layoutManager = LinearLayoutManager(this.activity) as RecyclerView.LayoutManager?
        view.rv_history.adapter = adapter
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
            deleteOneHistory(adapter.getRef(position).key!!) //get position id of rv_my_places
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
        history_list.child(myuser!!.uid).removeValue()
        adapter.notifyDataSetChanged()
    }
    private fun deleteOneHistory(key: String) {
        history_list.child(myuser!!.uid).child(key).removeValue()
        adapter.notifyDataSetChanged()
    }
    private fun uploadDatabase() {
        history_list.child(myuser!!.uid).push().setValue(historyDb)
    }
}
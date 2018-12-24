package com.horus.travelweather.activity

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.horus.travelweather.R
import com.horus.travelweather.common.TWConstant
import com.horus.travelweather.model.UserDbO
import com.rengwuxian.materialedittext.MaterialEditText
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_profile.view.*

class   ProfileFragment : Fragment() {


    private val TAG : String = ProfileFragment::class.toString()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_profile, container, false)

        view.txt_user_profile_name.text =  TWConstant.currentUser.name
        view.txt_phone_number.text = TWConstant.currentUser.phone
        view.txt_email_user.text = TWConstant.currentUser.email
        view.fabEdit.setOnClickListener {
            showDialog()
        }

        return view
    }


    companion object {
        fun newInstance(): ProfileFragment = ProfileFragment()
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        if(id == android.R.id.home) {
            context
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showDialog() {
        val alterDialog : AlertDialog.Builder = AlertDialog.Builder(context!!)
        alterDialog.setTitle("Edit Profile")

        val inflater : LayoutInflater = LayoutInflater.from(context!!)
        val dialogView =    inflater.inflate(R.layout.edit_profile_layout,null)
        alterDialog.setView(dialogView)
        val editName  = dialogView.findViewById<View>(R.id.editName) as MaterialEditText
        val editPhone = dialogView.findViewById<View>(R.id.editPhone) as MaterialEditText
        val editEmail = dialogView.findViewById<View>(R.id.editEmail) as MaterialEditText
        editName.setText(TWConstant.currentUser.name)
        editPhone.setText(TWConstant.currentUser.phone)
        editEmail.setText(TWConstant.currentUser.email)

        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val table_user : DatabaseReference = database.getReference("users")

        val mAuth : FirebaseAuth = FirebaseAuth.getInstance()
        alterDialog.setPositiveButton(getString(R.string.update)) { dialog, _ ->

            val user = UserDbO(editName.text.toString(),editEmail.text.toString(),editPhone.text.toString())
            table_user.child(mAuth.uid!!).setValue(user)
            Log.e(TAG, "UPDATE");

        }
        alterDialog.setNegativeButton(getString(R.string.cancel), { dialog, whichButton ->


        })
        alterDialog.create().show()

    }
}
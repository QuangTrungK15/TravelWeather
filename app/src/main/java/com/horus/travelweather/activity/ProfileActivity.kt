package com.horus.travelweather.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.horus.travelweather.R
import com.horus.travelweather.common.TWConstant
import com.horus.travelweather.model.UserDbO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.rengwuxian.materialedittext.MaterialEditText
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {


    private val TAG : String = MainActivity::class.toString()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)





        txt_user_profile_name.text =  TWConstant.currentUser.name
        txt_phone_number.text = TWConstant.currentUser.phone
        txt_email_user.text = TWConstant.currentUser.email



        fabEdit.setOnClickListener {
            showDialog();
        }





    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun showDialog() {
        val alterDialog : AlertDialog.Builder = AlertDialog.Builder(this)
        alterDialog.setTitle("Edit Profile")

        val inflater : LayoutInflater = LayoutInflater.from(this)
        val dialogView =    inflater.inflate(R.layout.edit_profile_layout,null)
        alterDialog.setView(dialogView)


        val editName  = dialogView.findViewById<View>(R.id.editName) as MaterialEditText
        val editPhone = dialogView.findViewById<View>(R.id.editPhone) as MaterialEditText
        val editEmail = dialogView.findViewById<View>(R.id.editEmail) as MaterialEditText


        editName.setText(TWConstant.currentUser.name)
        editPhone.setText(TWConstant.currentUser.phone)
        editEmail.setText(TWConstant.currentUser.email)


        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val table_user : DatabaseReference = database.getReference("user")





        val mAuth : FirebaseAuth = FirebaseAuth.getInstance()





        alterDialog.setPositiveButton("Update", { dialog, whichButton ->



            val user = UserDbO(editName.text.toString(),editEmail.text.toString(),editPhone.text.toString())
            table_user.child(mAuth.uid!!).setValue(user)
            Log.e(TAG, "UPDATE");

        })
        alterDialog.setNegativeButton("Cancel", { dialog, whichButton ->



        })


        alterDialog.create().show()

    }


    override fun onPause() {
        super.onPause()
        Log.e(TAG, "onPause");
    }


    override fun onStop() {
        super.onStop()
        Log.e(TAG, "onStop");
    }
}
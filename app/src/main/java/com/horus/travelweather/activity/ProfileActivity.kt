
package com.horus.travelweather.activity

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.horus.travelweather.R
import com.horus.travelweather.common.TWConstant
import com.horus.travelweather.model.UserDbO
import com.rengwuxian.materialedittext.MaterialEditText
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*

class   ProfileActivity : AppCompatActivity() {
    private val TAG : String = ProfileActivity::class.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        txt_user_profile_name.text =  TWConstant.currentUser.name
        txt_phone_number.text = TWConstant.currentUser.phone
        txt_email_user.text = TWConstant.currentUser.email
        Picasso.with(this).load(TWConstant.currentUser.urlPhoto).into(header_cover_image)
        fabEdit.setOnClickListener {
            showDialog()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        if(id == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showDialog() {
        val alterDialog : AlertDialog.Builder = AlertDialog.Builder(this)
        alterDialog.setTitle("Edit Profile")

        val actionBar1 = supportActionBar

        if (actionBar1 != null) {
            //actionBar1.setDisplayHomeAsUpEnabled(true)
            actionBar1.title = "Edit Profile"
        }

        val inflater : LayoutInflater = LayoutInflater.from(this)
        val dialogView =    inflater.inflate(R.layout.edit_profile_layout,null)
        alterDialog.setView(dialogView)
        val editName  = dialogView.findViewById<View>(R.id.editName) as MaterialEditText
        val editPhone = dialogView.findViewById<View>(R.id.editPhone) as MaterialEditText
        val editPassword = dialogView.findViewById<View>(R.id.editPassword) as MaterialEditText
        editName.setText(TWConstant.currentUser.name)
        editPhone.setText(TWConstant.currentUser.phone)
        editPassword.setText("")

        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val table_user : DatabaseReference = database.getReference("users")

        val mAuth : FirebaseAuth = FirebaseAuth.getInstance()
        alterDialog.setPositiveButton(getString(R.string.update)) { dialog, _ ->

            val user = UserDbO(editName.text.toString(),TWConstant.currentUser.email,editPhone.text.toString(),TWConstant.currentUser.urlPhoto)
            if(table_user.child(mAuth.uid!!).setValue(user).isSuccessful)
            {
                Toast.makeText(this@ProfileActivity,"UPDATED" , Toast.LENGTH_SHORT).show()
            }
            if(editPassword.text.toString().trim()!="")
            {
                val newUser = FirebaseAuth.getInstance().currentUser
                val newPassword = editPassword.text.toString()
                if(newPassword.trim() != "")
                {
                    if(newUser?.updatePassword(newPassword)!!.isSuccessful)
                    {
                        Toast.makeText(this@ProfileActivity,"User password updated." , Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        alterDialog.setNegativeButton(getString(R.string.cancel), { dialog, whichButton ->


        })
        alterDialog.create().show()
    }
}

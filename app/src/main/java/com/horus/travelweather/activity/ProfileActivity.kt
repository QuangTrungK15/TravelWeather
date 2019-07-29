
package com.horus.travelweather.activity

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.horus.travelweather.BottomNavigation
import com.horus.travelweather.R
import com.horus.travelweather.common.TWConstant
import com.horus.travelweather.model.UserDbO
import com.rengwuxian.materialedittext.MaterialEditText
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import uk.co.markormesher.android_fab.SpeedDialMenuAdapter
import uk.co.markormesher.android_fab.SpeedDialMenuItem

class   ProfileActivity : AppCompatActivity() {
    private val TAG : String = ProfileActivity::class.toString()
    private val flag = true;
    private lateinit var  progress : ProgressDialog ;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val actionBar1 = supportActionBar
        if (actionBar1 != null) {
            //actionBar1.setDisplayHomeAsUpEnabled(true)
            actionBar1.title = "Thông Tin Cá Nhân"
        }

        txt_user_profile_name.text =  TWConstant.currentUser.name
        txt_phone_number.text = TWConstant.currentUser.phone
        txt_email_user.text = TWConstant.currentUser.email
        Picasso.with(this).load(TWConstant.currentUser.urlPhoto).into(header_cover_image)
        fabEdit.speedDialMenuAdapter = speedDialMenuAdapter
        fabEdit.setContentCoverColour(Color.TRANSPARENT)
        progress = ProgressDialog(this)
        progress.setMessage("Loading....")
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        if(id == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showDialogChangeProfile() {
        val alterDialog : AlertDialog.Builder = AlertDialog.Builder(this)
        alterDialog.setTitle("Thay đổi thông tin")


/*if (actionBar1 != null) {
            //actionBar1.setDisplayHomeAsUpEnabled(true)
            actionBar1.title = "Thông Tin Cá Nhân"
        }*/

        val inflater : LayoutInflater = LayoutInflater.from(this)
        val dialogView =    inflater.inflate(R.layout.edit_profile_layout,null)
        alterDialog.setView(dialogView)
        val editName  = dialogView.findViewById<View>(R.id.editName) as MaterialEditText
        val editPhone = dialogView.findViewById<View>(R.id.editPhone) as MaterialEditText
        editName.setText(TWConstant.currentUser.name)
        editPhone.setText(TWConstant.currentUser.phone)

        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val table_user : DatabaseReference = database.getReference("users")

        val mAuth : FirebaseAuth = FirebaseAuth.getInstance()
        alterDialog.setPositiveButton("Cập Nhật") { dialog, _ ->

            val user = UserDbO(editName.text.toString(),TWConstant.currentUser.email,editPhone.text.toString(),TWConstant.currentUser.urlPhoto)

            table_user.child(mAuth.uid!!).setValue(user).addOnCompleteListener {
                if(it.isComplete)
                {
                    val intent = Intent(this, ProfileActivity::class.java) //this activity will be this fragment's father
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    Toast.makeText(this@ProfileActivity,"Thay đổi đã được cập nhật" , Toast.LENGTH_SHORT).show()
                }
            }
        }
        alterDialog.setNegativeButton("Hủy", { dialog, whichButton ->
            progress.dismiss()
        })
        alterDialog.create().show()
    }

    private fun showDialogChangePassword() {
        progress.show()
        val alterDialog : AlertDialog.Builder = AlertDialog.Builder(this)
        alterDialog.setTitle("Thay đổi mật khẩu")
        val inflater : LayoutInflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.edit_password_layout,null)
        val editPassword = dialogView.findViewById<View>(R.id.editPassword) as MaterialEditText
        alterDialog.setView(dialogView)
        editPassword.setText("")
        alterDialog.setPositiveButton("Cập nhật") { dialog, _ ->
            if(countOnString(editPassword.text.toString().trim()))
            {
                val newUser = FirebaseAuth.getInstance().currentUser
                val newPassword = editPassword.text.toString()
                if(newPassword.trim() != "")
                {
                    newUser?.updatePassword(newPassword)!!.addOnCompleteListener {
                        if(it.isComplete)
                        {
                            progress.dismiss()
                            val intent = Intent(this, ProfileActivity::class.java) //this activity will be this fragment's father
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                            Toast.makeText(this@ProfileActivity,"Mật khẩu đã được cập nhật" , Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
        alterDialog.setNegativeButton("Hủy", { dialog, whichButton ->
            progress.dismiss()
        })
        alterDialog.create().show()
    }


    private fun countOnString(text : String) : Boolean
    {
        if(text.count() >= 6)
        {
            return true
        }
        progress.dismiss()
        Toast.makeText(this@ProfileActivity,"Mật khẩu phải lớn hơn hoặc bằng 6 ký tự" , Toast.LENGTH_LONG).show()
        return false
    }

    private val speedDialMenuAdapter = object: SpeedDialMenuAdapter() {
        override fun getCount(): Int {
            return 2
        }

        override fun getMenuItem(context: Context, position: Int): SpeedDialMenuItem = when (position) {
            0 -> SpeedDialMenuItem(context, R.drawable.ic_edit_white_24dp, "Chỉnh sửa profile")
            1 -> SpeedDialMenuItem(context, R.drawable.ic_vpn_key_white_24dp, "Thay đổi mật khẩu")
            else -> throw IllegalArgumentException("No menu item: $position")
        }

        override fun onPrepareItemLabel(context: Context, position: Int, label: TextView) {
            // make the first item bold if there are multiple items
            // (this isn't a design pattern, it's just to demo the functionality)
                label.setTypeface(label.typeface, Typeface.BOLD)
                label.setTextColor(Color.WHITE)
        }

        override fun onMenuItemClick(position: Int): Boolean {
            if(position == 0)
            {
                showDialogChangeProfile()
                return true
            }
            else if(position == 1)
            {
                showDialogChangePassword()
                return true

            }
            return super.onMenuItemClick(position)
        }
        //        // rotate the "+" icon only
//        override fun fabRotationDegrees(): Float = if (0 == 0) 135F else 0F
    }

}

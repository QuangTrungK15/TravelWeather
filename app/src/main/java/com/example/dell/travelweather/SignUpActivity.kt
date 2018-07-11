package com.example.dell.travelweather

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.google.firebase.database.*
import com.rengwuxian.materialedittext.MaterialEditText
import android.widget.Toast
import com.google.android.gms.common.oob.SignUp
import com.example.dell.travelweather.R.id.editPhone
import com.example.dell.travelweather.R.id.editPass
import com.example.dell.travelweather.R.id.editName
import com.example.dell.travelweather.Model.User
import java.nio.file.Files.exists



class SignUpActivity : AppCompatActivity() {


    lateinit var editPhone: MaterialEditText
    lateinit var editPass: MaterialEditText
    lateinit var editName: MaterialEditText
    lateinit var btnSignUp: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)


        val database: FirebaseDatabase = FirebaseDatabase.getInstance()

        val table_user: DatabaseReference = database.getReference("User")



        editPhone = findViewById(R.id.editPhone)
        editName = findViewById(R.id.editName)
        editPass = findViewById(R.id.editPass)

        btnSignUp = findViewById(R.id.btnSignUp)



        btnSignUp.setOnClickListener {


            table_user.addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {

                    if (p0.child(editPhone.text.toString()).exists()) {

                        Toast.makeText(this@SignUpActivity, "Phone Number already register ", Toast.LENGTH_SHORT).show()


                    } else {

                        //get value to elements
                        val user = User(editName.text.toString(), editPass.text.toString())

                        //Sign up user account
                        table_user.child(editPhone.text.toString()).setValue(user)
                        Toast.makeText(this@SignUpActivity, "Sign up successfull ! ", Toast.LENGTH_SHORT).show()
                        finish()

                    }


                }


            })



        }






    }
}

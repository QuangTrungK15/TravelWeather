package com.example.dell.travelweather

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.rengwuxian.materialedittext.MaterialEditText
import com.google.firebase.database.DatabaseError
import android.widget.Toast
import android.content.Intent
import com.example.dell.travelweather.Common.Common
import com.example.dell.travelweather.Model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener






class MainActivity : AppCompatActivity() {

    lateinit var btnLogin: Button
    lateinit var btnSignUp: Button
    lateinit var editPhone: MaterialEditText
    lateinit var editPassword: MaterialEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val database: FirebaseDatabase = FirebaseDatabase.getInstance()

        val table_user: DatabaseReference = database.getReference("User")

        btnLogin = findViewById(R.id.btnLogin)
        btnSignUp = findViewById(R.id.btnSignUp)

        editPhone = findViewById(R.id.txtPhone)
        editPassword = findViewById(R.id.txtPassword)




        btnSignUp.setOnClickListener {
            val signUpIntent = Intent(this@MainActivity, SignUpActivity::class.java)
            startActivity(signUpIntent)
        }

        btnLogin.setOnClickListener {

             // Attach a listener to read the data at our posts reference
            table_user.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {

                    if(p0.child(editPhone.text.toString()).exists())
                    {


                        val user = p0.child(editPhone.text.toString()).getValue(User::class.java)

                        //Log.e("AAA" , ""+ editPhone.text.toString())

                        user!!.phone = editPhone.text.toString()

                        if (user.password == editPassword.text.toString()) {
                            val homeIntent = Intent(this@MainActivity, Home::class.java)
                            Common.currentUser = user
                            startActivity(homeIntent)
                            finish()

                        } else {
                            Toast.makeText(this@MainActivity, "Sign In failed !!!", Toast.LENGTH_SHORT).show()
                        }

                    }
                    else
                        Toast.makeText(this@MainActivity, "User not exits in database !!!", Toast.LENGTH_SHORT).show()

                }
            })
        }


    }
}

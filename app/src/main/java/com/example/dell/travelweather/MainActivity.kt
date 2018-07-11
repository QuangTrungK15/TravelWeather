package com.example.dell.travelweather

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.rengwuxian.materialedittext.MaterialEditText
import com.google.firebase.database.DatabaseError
import android.widget.Toast
import android.content.Intent
import com.example.dell.travelweather.Common.Common
import com.example.dell.travelweather.Model.User
import java.nio.file.Files.exists
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener



class MainActivity : AppCompatActivity() {

    lateinit var btnLogin : Button
    lateinit var editUserName : MaterialEditText
    lateinit var editPassword : MaterialEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val database : FirebaseDatabase = FirebaseDatabase.getInstance()

        val table_user : DatabaseReference = database.getReference("User")

        btnLogin = findViewById(R.id.btnLogin)

        btnLogin.setOnClickListener {

            // Attach a listener to read the data at our posts reference
            table_user.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {


                    if (dataSnapshot.child(editUserName.getText().toString()).exists()) {
                        //get user information
                        //progressDialog.dismiss()
                        var user = dataSnapshot.child(editUserName.getText().toString()).getValue<User>(User::class.java)
                        user!!.Name = editUserName.getText().toString()

                        /*if (user!!.getPassword().equals(editPassword.text.toString())) {
                            val homeIntent = Intent(this@MainActivity, Home::class.java)
                            Common.currentUser = user
                            startActivity(homeIntent)
                            finish()
                            //Toast.makeText(SignIn.this, ""+user.getPhone(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this@MainActivity, "Sign In failed !!!", Toast.LENGTH_SHORT).show()
                        }*/
                    } else {
                        //progressDialog.dismiss()
                        Toast.makeText(this@MainActivity, "User not exits in database !!!", Toast.LENGTH_SHORT).show()
                    }


                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
        }

    }









}

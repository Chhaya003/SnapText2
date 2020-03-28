package com.khemraj.snaptext

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    var emailEditText : EditText? = null
    var passwordEditText : EditText? = null
    val mAuth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setTitle("SnapShare Login")

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)

        if(mAuth.currentUser != null){
            login();
        }
    }


    fun loginClick(view: View){
        try{
            //check sign up
            mAuth.signInWithEmailAndPassword(emailEditText?.text.toString(), passwordEditText?.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        login()
                    } else {
                        // If sign in fails, display a message to the user.
                        mAuth.createUserWithEmailAndPassword(
                            emailEditText?.text.toString(),
                            passwordEditText?.text.toString()
                        )
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    //add to database
                                    FirebaseDatabase.getInstance().getReference().child("users").child(task.result!!.user?.uid!!).child("Email").setValue(emailEditText?.text.toString())
                                    login()
                                } else {
                                    Toast.makeText(this, "Login Failed, Try Again!", Toast.LENGTH_SHORT).show()
                                }

                            }
                    }
                }
        }catch (e : Exception){
            Toast.makeText(this, "Email or Password should not be blank", Toast.LENGTH_SHORT).show()
        }

    }

    fun login(){
        val intent  = Intent(this, SnapsActivity::class.java)
        finish()
        startActivity(intent)

    }
}

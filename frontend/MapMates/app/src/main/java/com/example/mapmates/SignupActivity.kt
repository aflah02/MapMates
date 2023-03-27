package com.example.mapmates

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import kotlin.math.sign

class SignupActivity : AppCompatActivity() {
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val loginRedirect: TextView = findViewById(R.id.login_redirect)
        val signupButton : Button = findViewById(R.id.sign_up)
        val userNameView : TextView = findViewById(R.id.username_field)
        val passwordView : TextView = findViewById(R.id.password_field)

        loginRedirect.setOnClickListener{
            val intent : Intent = Intent(applicationContext,LoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }

        signupButton.setOnClickListener {
            val userId : Int? = signUpUser(userNameView.text.toString(),passwordView.text.toString())
            if(userId == null){
                Toast.makeText(applicationContext,"Cannot Signup!", Toast.LENGTH_SHORT).show()
            }
            else{

                //Successful Sign-up
                val intent : Intent = Intent(applicationContext,MainActivity::class.java)
                intent.putExtra("UserId",userId)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
            }
        }
    }

    private fun signUpUser(username : String, password: String): Int? {
        //Make the API Call to sign up here, fetch the id on successful login else null
        return 1
    }

}
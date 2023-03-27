package com.example.mapmates

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class LoginActivity : AppCompatActivity() {

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginButton : Button = findViewById(R.id.log_in)
        val signupRedirect : TextView = findViewById(R.id.signup_redirect)
        val userNameView : TextView = findViewById(R.id.username_field)
        val passwordView : TextView = findViewById(R.id.password_field)

        signupRedirect.setOnClickListener{
            val intent : Intent = Intent(applicationContext,SignupActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }

        loginButton.setOnClickListener {
            val userId : Int? = logInUser(userNameView.text.toString(),passwordView.text.toString())
            if(userId == null){
                Toast.makeText(applicationContext,"Invalid Credentials!",Toast.LENGTH_SHORT).show()
            }
            else{
                //Successful Log-in
                val intent : Intent = Intent(applicationContext,MainActivity::class.java)
                intent.putExtra("UserId",userId)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
            }
        }
    }
    private fun logInUser(username : String, password: String): Int? {
        //Make the API Call to log in here, fetch the id on successful login else null
        if(username.isBlank() || password.isBlank())return null
        
        return 1
    }

}


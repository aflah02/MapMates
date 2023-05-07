package com.example.mapmates

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.mapmates.ui.people.friends.FriendsFragment
import com.squareup.picasso.Picasso
import okhttp3.*
import timber.log.Timber
import java.io.IOException

class FriendActivityProfile : AppCompatActivity() {

    private lateinit var tempName: TextView
    private lateinit var tempContact: TextView
    private lateinit var tempImage: ImageView
    private lateinit var tempButton: Button
    private var user = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_profile)
        //receive intent
        val intent = intent
        tempName = findViewById(R.id.friendProfileName)
        tempContact = findViewById(R.id.friendProfileContact)
        tempImage = findViewById(R.id.friendProfilePic)
        tempButton = findViewById(R.id.unfollowButton)
        val friendName = intent.getStringExtra("FRIEND_NAME")
        val friendContact = intent.getStringExtra("FRIEND_CONTACT")
        val friendProfilePic = intent.getStringExtra("FRIEND_PROFILE_PIC")
        tempName.text = friendName
        tempContact.text = friendContact
        Picasso.get().load(friendProfilePic).into(tempImage)
        val sharedPrefs = getSharedPreferences("Login", Context.MODE_PRIVATE)
        user = sharedPrefs.getString("Username",null).toString()
        if(user.isBlank()){
            //Run EntryActivity
            val intent = Intent(this, EntryActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }
        tempButton.setOnClickListener{
            // get the name of the friend whose request is being accepted
            val url = "https://mapsapp-1-m9050519.deta.app/users/$user/$friendName/removefriend"
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .post(RequestBody.create(null, ""))
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // handle network errors
                    Timber.tag("pendingf").e(e.message.toString())
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        onBackPressed()
                        }

                    else {
                        // handle unsuccessful response
                    }
            }
            })
        }

    }
}
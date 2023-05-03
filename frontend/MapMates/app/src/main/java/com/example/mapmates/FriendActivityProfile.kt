package com.example.mapmates

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso

class FriendActivityProfile : AppCompatActivity() {

    private lateinit var tempName: TextView
    private lateinit var tempContact: TextView
    private lateinit var tempImage: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_profile)
        //receive intent
        val intent = intent
        tempName = findViewById(R.id.friendProfileName)
        tempContact = findViewById(R.id.friendProfileContact)
        tempImage = findViewById(R.id.friendProfilePic)
        val friendName = intent.getStringExtra("FRIEND_NAME")
        val friendContact = intent.getStringExtra("FRIEND_CONTACT")
        val friendProfilePic = intent.getStringExtra("FRIEND_PROFILE_PIC")
        tempName.text = friendName
        tempContact.text = friendContact
        Picasso.get().load(friendProfilePic).into(tempImage)

    }
}
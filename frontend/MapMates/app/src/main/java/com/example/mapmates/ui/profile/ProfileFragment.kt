package com.example.mapmates.ui.profile

import com.squareup.picasso.Picasso

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.mapmates.R

class ProfileFragment : Fragment() {
    private lateinit var profilePicture: ImageView
    private lateinit var editBioButton: ImageButton
    private lateinit var name: TextView
    private lateinit var userName: TextView
    private lateinit var userBio: EditText



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        profilePicture = view.findViewById(R.id.profile_picture)
        name = view.findViewById(R.id.nameView)
        userName = view.findViewById(R.id.userNameView)
        userBio = view.findViewById(R.id.bioTextView)
        editBioButton = view.findViewById(R.id.editBioButton)
        Picasso.get().load("https://picsum.photos/200").into(profilePicture)
        editBioButton.setOnClickListener {
            userBio.isEnabled = true
//            userBio.ised
            userBio.isFocusableInTouchMode = true
            userBio.requestFocus()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        _binding = null
    }
}
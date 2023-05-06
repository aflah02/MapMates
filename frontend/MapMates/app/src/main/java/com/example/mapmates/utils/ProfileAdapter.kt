package com.example.mapmates.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mapmates.R

class ProfileAdapter(private val userNames : List<String>) : RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.user_profile, parent, false)
        return ProfileViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        holder.userName.text = userNames[position]
    }

    override fun getItemCount() = userNames.size

    inner class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val userName: TextView = itemView.findViewById(R.id.user_name)
    }
}
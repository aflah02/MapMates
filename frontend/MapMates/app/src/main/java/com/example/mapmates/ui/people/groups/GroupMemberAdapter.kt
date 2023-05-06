package com.example.mapmates.ui.people.groups

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mapmates.FriendActivityProfile
import com.example.mapmates.R
import com.example.mapmates.ui.people.friends.FriendData
import com.squareup.picasso.Picasso

class GroupMemberAdapter(var itemList: List<FriendData>) : RecyclerView.Adapter<GroupMemberAdapter.GrpMemberViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GrpMemberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.people_friends_row,parent,false)
        return GrpMemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: GrpMemberViewHolder, position: Int) {
        val currentItem = itemList[position]
        holder.contact_name.text = currentItem.name
        Picasso.get().load(currentItem.imageUrl).into(holder.profile_picture)
        holder.contact_number.text = currentItem.bio
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun setFilteredList(filteredList: List<FriendData>) {
        itemList = filteredList
        notifyDataSetChanged()
    }

    fun updateList(newList: List<FriendData>) {
        itemList = newList
        notifyDataSetChanged()
    }


    inner class GrpMemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profile_picture: ImageView = itemView.findViewById(R.id.profile_picture)
        val contact_name: TextView = itemView.findViewById(R.id.contact_name)
        val contact_number: TextView = itemView.findViewById(R.id.contact_number)
    }
}
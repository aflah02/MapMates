package com.example.mapmates.ui.people.friends

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.mapmates.FriendActivityProfile
import com.example.mapmates.R
import com.example.mapmates.SettingsActivity

import com.squareup.picasso.Picasso

class FriendsAdapter(var itemList: List<FriendData>) : RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.people_friends_row,parent,false)
        return FriendsViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendsViewHolder, position: Int) {
        val currentItem = itemList[position]
        holder.contact_name.text = currentItem.name
        Picasso.get().load(currentItem.imageUrl).into(holder.profile_picture)
        holder.contact_number.text = currentItem.bio
        holder.itemView.setOnClickListener{
            val context = holder.itemView.context
            val intent = Intent(context, FriendActivityProfile::class.java)
            intent.putExtra("FRIEND_NAME", holder.contact_name.text);
            intent.putExtra("FRIEND_PROFILE_PIC", currentItem.imageUrl);
            intent.putExtra("FRIEND_CONTACT",holder.contact_number.text)
            context.startActivity(intent)
        }
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


    inner class FriendsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profile_picture:  ImageView = itemView.findViewById(R.id.profile_picture)
        val contact_name: TextView = itemView.findViewById(R.id.contact_name)
        val contact_number: TextView = itemView.findViewById(R.id.contact_number)
    }
}
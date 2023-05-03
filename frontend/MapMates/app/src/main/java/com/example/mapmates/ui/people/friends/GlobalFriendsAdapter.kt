package com.example.mapmates.ui.people.friends

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mapmates.R
import com.squareup.picasso.Picasso


class GlobalFriendsAdapter(var searchResults: List<FriendData>): RecyclerView.Adapter<GlobalFriendsAdapter.GlobalFriendsViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GlobalFriendsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.people_friends_row, parent, false)
        return GlobalFriendsViewHolder(view)
    }

    override fun onBindViewHolder(holder: GlobalFriendsViewHolder, position: Int) {
        val currentItem = searchResults[position]
        holder.contact_name.text = currentItem.name
        Picasso.get().load(currentItem.imageUrl).into(holder.profile_picture)
        holder.contact_number.text = currentItem.number
    }

    override fun getItemCount(): Int {
        return searchResults.size
    }


    fun setFilteredList(filteredList: List<FriendData>) {
        searchResults = filteredList
        notifyDataSetChanged()
    }

    fun updateList(newList: List<FriendData>) {
        searchResults = newList
        notifyDataSetChanged()
    }

    inner class GlobalFriendsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profile_picture: ImageView = itemView.findViewById(R.id.profile_picture)
        val contact_name: TextView = itemView.findViewById(R.id.contact_name)
        val contact_number: TextView = itemView.findViewById(R.id.contact_number)
    }
}
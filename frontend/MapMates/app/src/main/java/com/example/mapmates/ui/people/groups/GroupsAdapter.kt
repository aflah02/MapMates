package com.example.mapmates.ui.people.groups

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mapmates.R
import com.example.mapmates.SettingsActivity

import com.squareup.picasso.Picasso

class GroupsAdapter(var itemList: List<GroupData>) : RecyclerView.Adapter<GroupsAdapter.GroupViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.people_groups_row,parent,false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val currentItem = itemList[position]
        holder.peopleGroupName.text = currentItem.title
        Picasso.get().load(currentItem.imageUrl).into(holder.peopleGroupImage)

        holder.peopleGroupSettings.setOnClickListener {
            // Handle button click for this item
            val context = holder.itemView.context
            val intent = Intent(context, SettingsActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun updateList(newList: List<GroupData>) {
        itemList = newList
        notifyDataSetChanged()
    }

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val peopleGroupName: TextView = itemView.findViewById(R.id.peopleGroupName)
        val peopleGroupImage: ImageView = itemView.findViewById(R.id.peopleGroupImage)
        val peopleGroupSettings: ImageButton = itemView.findViewById(R.id.peopleGroupSettings)

    }
}
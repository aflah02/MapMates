package com.example.mapmates.ui.home

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.mapmates.R
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso

class GroupsAdapter(private var groupsList: ArrayList<GroupModel>, private val listener: OnGroupItemClickListener) : RecyclerView.Adapter<GroupsAdapter.GroupsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_group_element, parent, false)
        return GroupsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: GroupsViewHolder, position: Int) {
        holder.groupName.text = groupsList[position].groupName
        holder.groupCount.text = groupsList[position].groupCount
        if(groupsList[position].groupId == "friends"){
            holder.groupImage.setImageDrawable(null)
        }
        else{
            Picasso.get().load("https://mapsapp-1-m9050519.deta.app/groups/${groupsList[position].groupId}/cover_image")
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                .fit().into(holder.groupImage)
        }
        holder.itemView.setOnClickListener {
            listener.onGroupItemClick(position);
        }
    }

    override fun getItemCount() = groupsList.size

    inner class GroupsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val groupName: TextView = itemView.findViewById(R.id.groupName)
        val groupCount: TextView = itemView.findViewById(R.id.groupCount)
        val groupImage: ImageView = itemView.findViewById(R.id.imageView)
        val groupElement: ConstraintLayout = itemView.findViewById(R.id.groupElement)
    }
}
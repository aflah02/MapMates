package com.example.mapmates.ui.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.mapmates.R

class GroupsAdapter(private val groupsList: ArrayList<GroupModel>, private val listener: OnItemClickListener) : RecyclerView.Adapter<GroupsAdapter.GroupsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_group_element, parent, false)
        return GroupsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: GroupsViewHolder, position: Int) {
        holder.groupName.text = groupsList[position].groupName
        holder.groupCount.text = groupsList[position].groupCount
        holder.groupImage.setImageResource(groupsList[position].groupImage)
        holder.itemView.setOnClickListener {
            listener.onItemClick(position)
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
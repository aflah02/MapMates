package com.example.mapmates.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mapmates.R

class GroupsAdapter(private val groupsList: ArrayList<String>, private val listener: OnItemClickListener) : RecyclerView.Adapter<GroupsAdapter.GroupsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_group_element, parent, false)
        return GroupsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: GroupsViewHolder, position: Int) {
        val currentItem = groupsList[position]
        holder.groupName.text = currentItem
        holder.itemView.setOnClickListener {
            listener.onItemClick(position)
        }
    }

    override fun getItemCount() = groupsList.size

    inner class GroupsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val groupName: TextView = itemView.findViewById(R.id.groupName)
    }
}
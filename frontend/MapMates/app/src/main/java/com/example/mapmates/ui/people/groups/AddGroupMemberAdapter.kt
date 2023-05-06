package com.example.mapmates.ui.people.groups

import com.example.mapmates.ui.people.friends.FriendData

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
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


class AddGroupMemberAdapter(var itemList: List<AddContactData>): RecyclerView.Adapter<AddGroupMemberAdapter.NewMemberViewHolder>() {
    private var orignalList = itemList.toMutableList()
    private var selectedContacts = mutableSetOf<AddContactData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewMemberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.add_contact_group_item,parent,false)
        return NewMemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewMemberViewHolder, position: Int) {
        val currentItem = itemList[position]
        holder.contact_name.text = currentItem.name
        Picasso.get().load(currentItem.imageUrl).into(holder.profile_picture)
        holder.contact_number.text = currentItem.number
//        holder.contact_selected.isChecked = selectedContacts.contains(currentItem)
        holder.contact_selected.isChecked = currentItem.isSelected
        holder.contact_selected.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedContacts.add(currentItem)
            } else {
                selectedContacts.remove(currentItem)
            }
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun setFilteredList(filteredList: List<AddContactData>) {
        itemList = filteredList
        notifyDataSetChanged()
    }

    fun updateList(newList: List<AddContactData>) {
        itemList = newList
        notifyDataSetChanged()
    }

    fun getSelectedContacts(): List<AddContactData> {
        return selectedContacts.toList()
    }

    inner class NewMemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profile_picture:  ImageView = itemView.findViewById(R.id.profile_picture)
        val contact_name: TextView = itemView.findViewById(R.id.contact_name)
        val contact_number: TextView = itemView.findViewById(R.id.contact_number)
        val contact_selected: CheckBox = itemView.findViewById(R.id.checkBoxSelected)
    }
}

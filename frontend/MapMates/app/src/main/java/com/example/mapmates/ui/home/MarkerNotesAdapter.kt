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

class MarkerNotesAdapter(private var notesList: ArrayList<String>, private var notesUploaderList: ArrayList<String>) : RecyclerView.Adapter<MarkerNotesAdapter.GroupsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.notes_marker_element, parent, false)
        return GroupsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: GroupsViewHolder, position: Int) {
        holder.authorName.text = notesUploaderList[position]
        holder.commentText.text = notesList[position]
    }

    override fun getItemCount() = notesList.size

    inner class GroupsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val commentText: TextView = itemView.findViewById(R.id.comment_text_view)
        val authorName: TextView = itemView.findViewById(R.id.author_text_view)
    }
}
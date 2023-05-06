package com.example.mapmates.ui.home

import android.graphics.Bitmap
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.example.mapmates.databinding.FragmentNotesBinding

import com.example.mapmates.ui.home.placeholder.PlaceholderContent.PlaceholderItem

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
class MyNotesRecyclerViewAdapter(
    val notes: List<String>,
    val uploader: List<Bitmap>,
    private val listener: OnGroupItemClickListener
) : RecyclerView.Adapter<MyNotesRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentNotesBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.noteView.text = notes[position]
        holder.uploaderView.setImageBitmap(uploader[position])
        holder.itemView.setOnClickListener {
            listener.onImageNoteClick(position, "note");
        }
    }

    override fun getItemCount(): Int = notes.size

    inner class ViewHolder(binding: FragmentNotesBinding) : RecyclerView.ViewHolder(binding.root) {
        val noteView : TextView = binding.note
        val uploaderView = binding.uploader
    }

}
package com.example.mapmates.ui.home

import android.graphics.Bitmap
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mapmates.databinding.FragmentImageBinding

import com.example.mapmates.ui.home.placeholder.PlaceholderContent.PlaceholderItem

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
class MyImageRecyclerViewAdapter(
    val images: MutableList<Bitmap>,
    val uploader: MutableList<Bitmap>,
    val uploaderNames: MutableList<String>,
    private val listener: OnGroupItemClickListener
) : RecyclerView.Adapter<MyImageRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.imageView.setImageBitmap(images[position])
        holder.uploaderView.setImageBitmap(uploader[position])
        holder.itemView.setOnClickListener(View.OnClickListener {
            listener.onImageNoteClick(position, "image");
        })
    }

    override fun getItemCount(): Int = images.size

    inner class ViewHolder(binding: FragmentImageBinding) : RecyclerView.ViewHolder(binding.root) {
        val imageView = binding.image
        val uploaderView = binding.uploader
    }

}